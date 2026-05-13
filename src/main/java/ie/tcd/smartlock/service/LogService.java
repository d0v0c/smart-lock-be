package ie.tcd.smartlock.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.mapper.LogMapper;
import ie.tcd.smartlock.model.entity.Log;
import org.springframework.stereotype.Service;

/**
 * @author xylingying
 * @description 针对表【log(日志)】的数据库操作Service
 * @createDate 2025-03-19 15:14:54
 */
@Service
public class LogService extends ServiceImpl<LogMapper, Log> {

}
