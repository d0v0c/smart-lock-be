package ie.tcd.smartlock.app.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.app.controller.vo.resp.AlertNotifyVO;
import ie.tcd.smartlock.app.entity.Alert;
import ie.tcd.smartlock.app.entity.Log;
import ie.tcd.smartlock.app.mapper.AlertMapper;
import ie.tcd.smartlock.app.mapper.UserDeviceMergeMapper;
import ie.tcd.smartlock.utils.BusinessException;
import ie.tcd.smartlock.utils.Result;
import ie.tcd.smartlock.utils.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xylingying
 * @description 针对表【alert(警报记录表)】的数据库操作Service实现
 * @createDate 2025-03-06 18:27:15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService extends ServiceImpl<AlertMapper, Alert> {
    private final UserDeviceMergeMapper userDeviceMapper;
    private final LogService logService;
    private final SimpMessageSendingOperations messagingTemplate;

    public void notifyAlert(Alert recvAlert, Long deviceId) {
        recvAlert.setDeviceId(deviceId);
        if (recvAlert.getAlertId() == null) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "AlertId cannot be null");
        }
        // 存储警报信息
        this.save(recvAlert);
        logService.save(new Log(Log.ActionType.ALERT_NOTIFY, recvAlert.toString()));

        // 通知用户警报信息
        AlertNotifyVO alertNotifyVO = new AlertNotifyVO();
        BeanUtils.copyProperties(recvAlert, alertNotifyVO);
        List<String> usernames = userDeviceMapper.selectUsernamesByDeviceId(deviceId);
        for (String username : usernames) {
            log.info("向用户 {} 报警", username);
            // 目的地拼成 /user/{username}/queue/alert
            messagingTemplate.convertAndSendToUser(username, "/queue/alert", Result.success(alertNotifyVO));
        }
    }
}