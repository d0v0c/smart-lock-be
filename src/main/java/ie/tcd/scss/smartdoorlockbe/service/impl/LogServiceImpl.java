package ie.tcd.scss.smartdoorlockbe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ie.tcd.scss.smartdoorlockbe.domain.Log;
import ie.tcd.scss.smartdoorlockbe.service.LogService;
import ie.tcd.scss.smartdoorlockbe.mapper.LogMapper;
import org.springframework.stereotype.Service;

/**
* @author xylingying
* @description 针对表【log(日志)】的数据库操作Service实现
* @createDate 2025-03-19 15:14:54
*/
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService{

}




