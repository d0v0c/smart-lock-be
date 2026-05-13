package ie.tcd.smartlock.mapper;

import ie.tcd.smartlock.model.entity.Log;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xylingying
 * @description 针对表【log(日志)】的数据库操作Mapper
 * @createDate 2025-03-19 15:14:54
 * @Entity ie.tcd.scss.smartdoorlockbe.domain.Log
 */
@Mapper
public interface LogMapper extends BaseMapper<Log> {

}




