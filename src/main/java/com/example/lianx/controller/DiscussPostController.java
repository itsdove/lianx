package com.example.lianx.controller;

import com.example.lianx.entity.*;
import com.example.lianx.service.CommentService;
import com.example.lianx.service.DiscussPostService;
import com.example.lianx.service.LikeService;
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

import java.util.*;


@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostholder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(int posttype,String title,String content){
        User user = hostholder.getUser();
        if(user==null) {
            return CommunityUtil.getJSONString(403,"没有登录");
        }
        DiscussPost discussPost=new DiscussPost();
        discussPost.setPosttype(posttype);
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        return CommunityUtil.getJSONString(0,"发布成功");
    }


    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        boolean logined;
        if(hostholder.getUser()==null)
            logined=false;
        else
            logined=true;
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        Long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);

        int likeStatus;
        if(logined)
            likeStatus=likeService.findEntityLikeStatus(hostholder.getUser().getId(),ENTITY_TYPE_POST, discussPostId);
        else
            likeStatus=0;
        model.addAttribute("likeStatus",likeStatus);

        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(discussPost.getCommentCount());

        List<Comment> commentList = commentService.findCommentByEntity(
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                Map<String,Object> commentVo=new HashMap<>();
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                if (logined)
                    likeStatus = likeService.findEntityLikeStatus(hostholder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                else
                    likeStatus = 0;
                commentVo.put("likeStatus",likeStatus);
                likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);


                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList=new ArrayList<>();

                if(replyList!=null){
                    for(Comment reply:replyList){
                        Map<String,Object> replyVo=new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        User target=reply.getTargetId()== 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        if (logined)
                            likeStatus = likeService.findEntityLikeStatus(hostholder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        else
                            likeStatus = 0;
                        replyVo.put("likeStatus",likeStatus);
                        likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                int repltCount=commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",repltCount);
                commentVoList.add(commentVo);
            }

        }

        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }


    // 置顶
    @PreAuthorize("hasAuthority('owner')")
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @PreAuthorize("hasAuthority('owner')")
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);
        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @PreAuthorize("hasAuthority('owner')")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        return CommunityUtil.getJSONString(0);
    }

}
