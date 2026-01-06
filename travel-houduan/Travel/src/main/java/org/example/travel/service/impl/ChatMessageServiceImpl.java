package org.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.travel.model.entity.ChatMessage;
import org.example.travel.service.ChatMessageService;
import org.example.travel.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 13763
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
* @createDate 2026-01-01 16:40:25
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




