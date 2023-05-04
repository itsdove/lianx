package com.example.lianx.controller;

import com.example.lianx.entity.DiscussPost;
import com.example.lianx.entity.Page;
import com.example.lianx.entity.User;
import com.example.lianx.service.DiscussPostService;
import com.example.lianx.service.LikeService;
import com.example.lianx.service.UserService;
import com.example.lianx.util.CommunityConstant;
import com.example.lianx.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService disscusPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String  getIndexPage(Model model, Page page, @RequestParam(name="posttype",defaultValue ="0")int posttype){
        page.setRows(disscusPostService.findDiscussPostRows(0,posttype));
        page.setPath("/index?posttype="+posttype);

        List<DiscussPost> list = disscusPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),posttype);
        List<Map<String,Object>> discussPosts =new ArrayList<>();
        if(list!=null){
            for(DiscussPost post:list){
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                User user=userService.findUserById(post.getUserId());
                map.put("user",user);
                Long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("posttype",posttype);
        return "index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String  getErrorPage() {
        return "error/404";
    }
}
