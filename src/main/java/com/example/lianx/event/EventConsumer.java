package com.example.lianx.event;

import com.alibaba.fastjson.JSONObject;
import com.example.lianx.entity.Event;
import com.example.lianx.entity.Message;
import com.example.lianx.service.MessageService;
import com.example.lianx.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    public static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;


    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void  handleCommentMessage(ConsumerRecord consumerRecord){
        if(consumerRecord==null||consumerRecord.value()==null){
            logger.error("空消息");
            return;
        }
        Event event = JSONObject.parseObject(consumerRecord.value().toString(), Event.class);
        if(event==null){
            logger.error("错误");
            return;
        }

        Message message=new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        message.setStatus(0);
        Map<String,Object> content=new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
