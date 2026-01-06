package org.example.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.travel.model.entity.ChatConversation;
import org.example.travel.service.ChatConversationService;
import org.example.travel.mapper.ChatConversationMapper;
import org.springframework.stereotype.Service;

/**
* @author 13763
* @description 针对表【chat_conversation(聊天会话表)】的数据库操作Service实现
* @createDate 2026-01-01 16:40:25
*/
@Service
public class ChatConversationServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation>
    implements ChatConversationService{

}




