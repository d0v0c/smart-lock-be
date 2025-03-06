package ie.tcd.scss.smartdoorlockbe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ie.tcd.scss.smartdoorlockbe.domain.UserDeviceMerge;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xylingying
 * @description 针对表【user_device_merge(用户与门禁的多对多关系)】的数据库操作Mapper
 * @createDate 2025-03-06 17:58:06
 * @Entity ie.tcd.scss.smartdoorlockbe.domain.UserDeviceMerge
 */
@Mapper
public interface UserDeviceMergeMapper extends BaseMapper<UserDeviceMerge> {

}




