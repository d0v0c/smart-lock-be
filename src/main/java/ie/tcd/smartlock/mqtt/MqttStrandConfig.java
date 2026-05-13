package ie.tcd.smartlock.mqtt;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.messaging.MessageChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * MQTT 入站消息的分发。线程分片绑定。
 * <p>
 * 把 deviceId 哈希到 strand 上，每个 strand = 单虚拟线程 + 有界队列；
 * 同一设备的消息一定走同一根 strand，从而保留设备内顺序。
 */
@Configuration
public class MqttStrandConfig {

    public static final int STRAND_COUNT = 16;
    public static final int STRAND_QUEUE_CAPACITY = 1000;

    private final List<ExecutorService> executors;
    private final List<MessageChannel> strandChannels;
    private final MessageChannel afterStrandChannel;

    public MqttStrandConfig() {
        List<ExecutorService> execs = new ArrayList<>(STRAND_COUNT);
        List<MessageChannel> chs = new ArrayList<>(STRAND_COUNT);

        // 队满时阻塞调用线程（Paho 的线程）
        RejectedExecutionHandler blockingPolicy = (r, exec) -> {
            try {
                // put() 是一个阻塞方法，等待把消息 r 塞进线程池 exec 的阻塞队列中
                exec.getQueue().put(r);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException(ie);
            }
        };

        for (int i = 0; i < STRAND_COUNT; i++) {
            // 创建虚拟线程
            ThreadFactory tf = Thread.ofVirtual()
                    .name("mqtt-strand-" + i + "-", 0)
                    .factory();
            ThreadPoolExecutor exec = new ThreadPoolExecutor(
                    1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(STRAND_QUEUE_CAPACITY),
                    tf,
                    blockingPolicy);
            execs.add(exec);
            // 创建 Channel
            ExecutorChannel ch = new ExecutorChannel(exec);
            ch.setBeanName("mqttStrandChannel-" + i);
            chs.add(ch);
        }

        this.executors = List.copyOf(execs);
        this.strandChannels = List.copyOf(chs);

        DirectChannel after = new DirectChannel();
        after.setBeanName("afterStrandChannel");
        this.afterStrandChannel = after;
    }

    public List<MessageChannel> getStrandChannels() {
        return strandChannels;
    }

    public MessageChannel getAfterStrandChannel() {
        return afterStrandChannel;
    }

    /**
     * 多 strandChannels 合并成一个 afterStrandChannel。（bridge转发）
     */
    @Bean
    public List<IntegrationFlowRegistration> strandBridges(IntegrationFlowContext flowContext) {
        List<IntegrationFlowRegistration> regs = new ArrayList<>(STRAND_COUNT);
        for (int i = 0; i < STRAND_COUNT; i++) {
            IntegrationFlow bridge = IntegrationFlow.from(strandChannels.get(i))
                    .bridge()
                    .channel(afterStrandChannel)
                    .get();
            regs.add(flowContext.registration(bridge)
                    .id("mqttStrandBridge-" + i)
                    .register());
        }
        return List.copyOf(regs);
    }

    @PreDestroy
    void shutdownStrands() {
        for (ExecutorService es : executors) {
            // 发通知SHUTDOWN，拒收新任务，队列里剩余的任务继续跑
            es.shutdown();
        }
        for (ExecutorService es : executors) {
            try {
                // 等了 10 秒还没全跑完
                if (!es.awaitTermination(10, TimeUnit.SECONDS)) {
                    // 强制中断
                    es.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                es.shutdownNow();
            }
        }
    }
}
