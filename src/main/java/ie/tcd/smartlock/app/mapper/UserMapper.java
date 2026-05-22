package ie.tcd.smartlock.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ie.tcd.smartlock.app.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xylingying
 * @description 针对表【user(后台用户表)】的数据库操作Mapper
 * @createDate 2025-03-05 16:04:41
 * @Entity ie.tcd.scss.smartdoorlockbe.domain.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




