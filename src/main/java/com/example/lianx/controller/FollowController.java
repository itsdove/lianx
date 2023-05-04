package com.example.lianx.controller;

import com.example.lianx.entity.Event;
import com.example.lianx.entity.Page;
import com.example.lianx.entity.User;
import com.example.lianx.event.EventProducer;
import com.example.lianx.service.FollowService;
import com.example.lianx.service.UserService;
import com.example.lianx.util.CommunityConstant;
import com.example.lianx.util.CommunityUtil;
import com.example.lianx.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path="/follow",method= RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user=hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);

        Event event=new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注");
    }

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path="/unfollow",method= RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user=hostHolder.getUser();
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }


    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model model){
        User userById = userService.findUserById(userId);
        if(userById==null){
            throw new RuntimeException();
        }
        model.addAttribute("user",userById);
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(followees!=null){
            for(Map<String,Object>map:followees){
                User user = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users",followees);
        return "site/followee";
    }

    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model model){
        User userById = userService.findUserById(userId);
        if(userById==null){
            throw new RuntimeException();
        }
        model.addAttribute("user",userById);
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(followers!=null){
            for(Map<String,Object>map:followers){
                User user = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users",followers);
        return "site/followee";
    }

    private boolean hasFollowed(int userId){
        if(hostHolder.getUser()==null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }
}
