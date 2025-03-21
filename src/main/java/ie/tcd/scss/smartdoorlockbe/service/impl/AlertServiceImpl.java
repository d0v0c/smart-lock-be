package ie.tcd.scss.smartdoorlockbe.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.scss.smartdoorlockbe.domain.Alert;
import ie.tcd.scss.smartdoorlockbe.domain.Log;
import ie.tcd.scss.smartdoorlockbe.domain.UserDeviceMerge;
import ie.tcd.scss.smartdoorlockbe.mapper.AlertMapper;
import ie.tcd.scss.smartdoorlockbe.service.AlertService;
import ie.tcd.scss.smartdoorlockbe.service.LogService;
import ie.tcd.scss.smartdoorlockbe.service.UserDeviceMergeService;
import ie.tcd.scss.smartdoorlockbe.utils.BusinessException;
import ie.tcd.scss.smartdoorlockbe.utils.Result;
import ie.tcd.scss.smartdoorlockbe.utils.StatusCode;
import ie.tcd.scss.smartdoorlockbe.vo.resp.AlertNotifyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xylingying
 * @description 针对表【alert(警报记录表)】的数据库操作Service实现
 * @createDate 2025-03-06 18:27:15
 */
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {
    @Autowired
    private UserDeviceMergeService userDeviceMergeService;
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private LogService logService;

    @Override
    public void notifyAlert(String payload) {
        Alert recvAlert = JSON.parseObject(payload, Alert.class);
        if (recvAlert.getAlertId() != null) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "deviceId cannot be null");
        }
        // 存储警报信息
        LambdaQueryWrapper<UserDeviceMerge> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(UserDeviceMerge::getUsername);
        lambdaQueryWrapper.eq(UserDeviceMerge::getDeviceId, recvAlert.getDeviceId());
        List<UserDeviceMerge> list = userDeviceMergeService.list(lambdaQueryWrapper);

        AlertNotifyVO alertNotifyVO = new AlertNotifyVO();
        BeanUtils.copyProperties(recvAlert, alertNotifyVO);

        this.save(recvAlert);
        logService.save(new Log(Log.ActionType.ALERT_NOTIFY, payload));
        // 通知用户警报信息
        for (UserDeviceMerge userDeviceMerge : list) {
            System.out.println("向用户" + userDeviceMerge.getUsername() + "报警");
            messagingTemplate.convertAndSendToUser(userDeviceMerge.getUsername(), "/queue/alert", Result.success(alertNotifyVO));
        }

    }
}




