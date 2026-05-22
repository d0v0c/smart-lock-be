package ie.tcd.smartlock.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ie.tcd.smartlock.app.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author xylingying
 * @description 针对表【device(ESP32设备表)】的数据库操作Mapper
 * @createDate 2025-02-21 00:58:41
 * @Entity ie.tcd.scss.smartdoorlockbe.domain.Device
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    @Select("""
            SELECT d.*
            FROM device d
            INNER JOIN user_device_merge udm ON udm.device_id = d.device_id
            WHERE udm.username = #{username}
            """)
    List<Device> selectByUsername(String username);
}




