package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author sc
 * @date 2023-06-14下午 08:57
 */
@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表,针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);
    //查询当前用户的会话数量
    int selectConversationsCount(int userId);
    //查询某个会话包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);
    //查询某个会话包含的私信数量
    int selectLetterCount(String conversationId);
    //查询未读的私信数量
    int selectLetterUnReadCount(int userId,String conversationId);
    //增加私信
    int insertMessage(Message message);
    //
    int updateStatus(List<Integer> ids,int status);
    @Select({"select id,from_id,to_id,conversation_id,content,status,create_time",
            "from message where id =#{id}"})
    Message findMessageById(Integer id);
}
