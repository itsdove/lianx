package com.example.lianx.mapper;

import com.example.lianx.entity.Message;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageMapper {

    List<Message> selectConversation(int userId,int offset,int limit);

    int selectConversationCount(int userId);

    List<Message> selectLetters(String conversationId,int offset,int limit);

    int selectLetterCount(String conversation);

    int selectLetterUnreadCount(int userId,String conversationId);

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids,int status);

    Message selectLatestNotice(int userId,String topic);

    int selectNoticeCount(int userId,String topic);

    int selectNoticeUnreadCount(int userId,String topic);

    List<Message> selectNotice(int userId,String topic,int offset,int limit);
}