package org.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.travel.model.entity.UserBehaviorLog;
import org.example.travel.service.UserBehaviorLogService;
import org.example.travel.mapper.UserBehaviorLogMapper;
import org.springframework.stereotype.Service;

/**
* @author 13763
* @description 针对表【user_behavior_log(用户行为日志表)】的数据库操作Service实现
* @createDate 2026-01-01 16:40:25
*/
@Service
public class UserBehaviorLogServiceImpl extends ServiceImpl<UserBehaviorLogMapper, UserBehaviorLog>
    implements UserBehaviorLogService{

}




