package ie.tcd.scss.smartdoorlockbe.mapper;

import ie.tcd.scss.smartdoorlockbe.domain.Device;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author xylingying
* @description 针对表【device(ESP32设备表)】的数据库操作Mapper
* @createDate 2025-02-21 00:58:41
* @Entity ie.tcd.scss.smartdoorlockbe.domain.Device
*/
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

}




