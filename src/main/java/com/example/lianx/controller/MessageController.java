package com.example.lianx.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.lianx.entity.Message;
import com.example.lianx.entity.Page;
import com.example.lianx.entity.User;
import com.example.lianx.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        List<Message> conversationList = messageService.findConversation(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> LetterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letters != null) {
            for (Message message : LetterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        model.addAttribute("target",getLetterTarget(conversationId));

        List<Integer> letterIds = getLetterIds(LetterList);
        if(!letterIds.isEmpty()){
            messageService.readMessage(letterIds);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids=new ArrayList<>();
        if(!letterList.isEmpty()){
            for(Message message:letterList){
                User user = hostHolder.getUser();
                if(hostHolder.getUser().getId().equals(message.getToId())&&message.getStatus().equals(0)){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message=new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path = "/notice/list",method=RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        Message message=messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        Map<String,Object> messagevo=new HashMap<>();
        if(message!=null){
            messagevo.put("message",message);
            String content= HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messagevo.put("user",userService.findUserById((Integer) data.get("userId")));
            messagevo.put("entityId",data.get("entityId"));
            messagevo.put("entityType",data.get("entityType"));
            messagevo.put("postId",data.get("postId"));

            int count =messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messagevo.put("count",count);
            int unread=messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messagevo.put("unread",unread);
            model.addAttribute("commentNotice",messagevo);
        }



         message=messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
         messagevo=new HashMap<>();
        if(message!=null){
            messagevo.put("message",message);
            String content= HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messagevo.put("user",userService.findUserById((Integer) data.get("userId")));
            messagevo.put("entityId",data.get("entityId"));
            messagevo.put("entityType",data.get("entityType"));
            messagevo.put("postId",data.get("postId"));

            int count =messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messagevo.put("count",count);
            int unread=messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messagevo.put("unread",unread);
            model.addAttribute("likeNotice",messagevo);
        }


         message=messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
         messagevo=new HashMap<>();
        if(message!=null){
            messagevo.put("message",message);
            String content= HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messagevo.put("user",userService.findUserById((Integer) data.get("userId")));
            messagevo.put("entityId",data.get("entityId"));
            messagevo.put("entityType",data.get("entityType"));

            int count =messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messagevo.put("count",count);
            int unread=messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messagevo.put("unread",unread);
            model.addAttribute("followNotice",messagevo);
        }


        int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path = "/notice/detail/{topic}",method=RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic")String topic ,Page page ,Model model){
        User user=hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList=new ArrayList<>();
        if(noticeList!=null){
            for(Message notice:noticeList){
                Map<String,Object> map=new HashMap<>();
                map.put("notice",notice);
                String content=HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data=JSONObject.parseObject(content);
                map.put("user",user);
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        List<Integer> letterIds = getLetterIds(noticeList);
        if(!letterIds.isEmpty()){
            messageService.readMessage(letterIds);
        }

        return "/site/notice-detail";

    }
}
