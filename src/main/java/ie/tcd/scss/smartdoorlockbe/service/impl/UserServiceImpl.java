package ie.tcd.scss.smartdoorlockbe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.scss.smartdoorlockbe.domain.User;
import ie.tcd.scss.smartdoorlockbe.mapper.UserMapper;
import ie.tcd.scss.smartdoorlockbe.service.UserService;
import ie.tcd.scss.smartdoorlockbe.utils.BusinessException;
import ie.tcd.scss.smartdoorlockbe.utils.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author xylingying
 * @description 针对表【user(后台用户表)】的数据库操作Service实现
 * @createDate 2025-03-05 16:04:41
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void register(User request) {
        // 查询用户名是否重复
        // @Select("SELECT username FROM user WHERE username = #{username}")
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(User::getUsername)
                .eq(User::getUsername, request.getUsername());
        User user = this.getOne(lambdaQueryWrapper);
        if (user != null) {
            throw new BusinessException(StatusCode.VALIDATION_ERROR, "The account already exists");
        }
        // 加密登录密码
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        boolean ret = this.save(request);
        if (!ret) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Failed to insert data");
        }
    }

    @Override
    public void update(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean updated = this.updateById(user);
        if (!updated) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "Failed to update data");
        }
    }

    @Override
    public boolean resetPassword(User user) {
        User storedUser = this.getById(user.getUsername());
        if (storedUser == null) {
            throw new BusinessException(StatusCode.ACCOUNT_NOT_FOUND, "The username does not exist");
        }
        if (!storedUser.getEmail().equals(user.getEmail())) {
            throw new BusinessException(StatusCode.EMAIL_NOT_MATCH, "Email address does not match the username");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.updateById(user);
    }

//    @Override
//    public void login(User request) {
//        // 查询用户名是否存在
//        // @Select("SELECT username, password FROM user WHERE username = #{username}")
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.select(User::getUsername, User::getPassword)
//                .eq(User::getUsername, request.getUsername());
//        User user = this.getOne(lambdaQueryWrapper);
//        if (user == null) {
//            throw new BusinessException(StatusCode.VALIDATION_ERROR, "用户不存在");
//        }
//        if (!user.getPassword().equals(request.getPassword())) {
//            throw new BusinessException(StatusCode.VALIDATION_ERROR, "密码错误");
//        }
//    }
}




