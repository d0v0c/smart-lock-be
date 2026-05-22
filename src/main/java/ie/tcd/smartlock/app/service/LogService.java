package ie.tcd.smartlock.app.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.smartlock.app.entity.Log;
import ie.tcd.smartlock.app.mapper.LogMapper;
import org.springframework.stereotype.Service;

/**
 * @author xylingying
 * @description 针对表【log(日志)】的数据库操作Service
 * @createDate 2025-03-19 15:14:54
 */
@Service
public class LogService extends ServiceImpl<LogMapper, Log> {

}
