package ie.tcd.scss.smartdoorlockbe.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ie.tcd.scss.smartdoorlockbe.domain.Alert;

/**
 * @author xylingying
 * @description 针对表【alert(警报记录表)】的数据库操作Service
 * @createDate 2025-03-06 18:27:15
 */
public interface AlertService extends IService<Alert> {
    void notifyAlert(String payload);
}
