package com.example.lianx.controller;

import com.example.lianx.entity.Event;
import com.example.lianx.entity.User;
import com.example.lianx.event.EventProducer;
import com.example.lianx.service.LikeService;
import com.example.lianx.util.CommunityConstant;
import com.example.lianx.util.CommunityUtil;
import com.example.lianx.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(method = RequestMethod.POST,path = "/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user=hostHolder.getUser();
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        long likeCount=likeService.findEntityLikeCount(entityType,entityId);
        int likeStatus=likeService.findEntityLikeStatus(user.getId(),entityType,entityId);
        Map<String,Object> map=new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        if(likeStatus==1){
            Event event=new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}
