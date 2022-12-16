package com.example.lianx.service;

import com.example.lianx.entity.Message;
import com.example.lianx.mapper.MessageMapper;
import com.example.lianx.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversation(int userId, int offset, int limit){
      return   messageMapper.selectConversation(userId, offset,limit);
    }

    public int findConversationCount(int userId){
        return  messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return  messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int findLetterCount(String conversation){
        return messageMapper.selectLetterCount(conversation);
    };

    public int findLetterUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setStatus(0);
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

    public Message findLatestNotice(int userId, String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }

    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }

    public int findNoticeUnreadCount(int userId, String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    public List<Message> findNotices(int userId,String topic,int offset,int limit){
        return messageMapper.selectNotice(userId,topic,offset,limit);
    }

}
