package com.nowcoder.community.service;


import com.nowcoder.community.entity.Message;

import java.util.List;

/**
* @author 17136
* @description 针对表【message】的数据库操作Service
* @createDate 2023-06-14 21:18:14
*/
public interface MessageService{

    List<Message> findConversations(int userId, int offset, int limit);

    int findConversationsCount(int userId);

    List<Message> findLetters(String conversationId, int offset, int limit);

    int findLetterCount(String conversationId);

    int findLetterUnReadCount(int userId, String conversationId);

    int addNessage(Message message);


    int readMessage(List<Integer> ids);
}
