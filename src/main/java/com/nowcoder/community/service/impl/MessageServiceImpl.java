package com.nowcoder.community.service.impl;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.SensitiveFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author 17136
 * @description 针对表【message】的数据库操作Service实现
 * @createDate 2023-06-14 21:18:14
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {


    private final MessageMapper messageMapper;
    private final SensitiveFilter sensitiveFilter;

    @Override
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    @Override
    public int findConversationsCount(int userId) {
        return messageMapper.selectConversationsCount(userId);
    }

    @Override
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    @Override
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    @Override
    public int findLetterUnReadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnReadCount(userId, conversationId);
    }

    @Override
    public int addNessage(Message message) {
        message.setStatus(0);
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    @Override
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    @Override
    public int deleteMessage(Integer id) {
        return messageMapper.updateStatus(Collections.singletonList(id), 2);
    }
    @Override
    public Message findMessageById(Integer id) {
        return messageMapper.findMessageById(id);
    }
}





