package ie.tcd.smartlock.mapper;

import ie.tcd.smartlock.model.entity.AccessCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xylingying
 * @description 针对表【access_code(门锁密码表)】的数据库操作Mapper
 * @createDate 2025-02-22 16:15:31
 * @Entity ie.tcd.scss.smartdoorlockbe.domain.AccessCode
 */
@Mapper
public interface AccessCodeMapper extends BaseMapper<AccessCode> {

}




