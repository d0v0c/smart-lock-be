package ie.tcd.scss.smartdoorlockbe.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ie.tcd.scss.smartdoorlockbe.domain.AccessCode;

import java.time.ZonedDateTime;

/**
 * @author xylingying
 * @description 针对表【access_code(门锁密码表)】的数据库操作Service
 * @createDate 2025-02-22 16:15:31
 */
public interface AccessCodeService extends IService<AccessCode> {
    String generateCode(Long deviceId, ZonedDateTime from, ZonedDateTime to, String owner);

    void confirmUpdate(String payload);

    void getAllAccessCode(String payload);
}
