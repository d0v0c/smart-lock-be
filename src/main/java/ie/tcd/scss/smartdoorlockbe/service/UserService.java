package ie.tcd.scss.smartdoorlockbe.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ie.tcd.scss.smartdoorlockbe.domain.User;

/**
 * @author xylingying
 * @description 针对表【user(后台用户表)】的数据库操作Service
 * @createDate 2025-03-05 16:04:41
 */
public interface UserService extends IService<User> {
    void register(User user);

    String login(User user);
}
