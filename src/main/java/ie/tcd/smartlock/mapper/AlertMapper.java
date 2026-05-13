package ie.tcd.smartlock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ie.tcd.smartlock.model.entity.Alert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xylingying
 * @description 针对表【alert(警报记录表)】的数据库操作Mapper
 * @createDate 2025-03-06 18:27:15
 * @Entity ie.tcd.scss.smartdoorlockbe.domain.Alert
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

}




