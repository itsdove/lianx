package com.example.lianx.controller;

import com.example.lianx.annotation.LoginRequired;
import com.example.lianx.entity.DiscussPost;
import com.example.lianx.entity.Page;
import com.example.lianx.entity.ReplyPostResult;
import com.example.lianx.entity.User;
import com.example.lianx.service.*;
import com.example.lianx.util.CommunityConstant;
import com.example.lianx.util.CommunityUtil;
import com.example.lianx.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path="/setting",method= RequestMethod.GET)
    public String getSettingPage(){
        return "site/setting";
    }


    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(path="/upload",method= RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        if(StringUtils.isBlank(substring)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }

       originalFilename = CommunityUtil.generateUUID() + substring;

        File dest=new File(uploadPath+"/"+originalFilename);

        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("服务器异常"+e);
        }
        User user=hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+originalFilename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @RequestMapping(path="/header/{filename}",method= RequestMethod.GET)
    public void getHeader(@PathVariable("filename")String fileName, HttpServletResponse response){
        fileName=uploadPath+"/"+fileName;
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/"+suffix);

        try (
            OutputStream outputStream = response.getOutputStream();
            FileInputStream fileInputStream=new FileInputStream(fileName);
        ){
            byte[] buffer=new byte[1024];
            int b=0;
            while((b=fileInputStream.read(buffer))!=-1){
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public  String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user= userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user",user);
        int likeCount =likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        boolean hasFollowed=false;
        if(hostHolder.getUser()!=null){
            hasFollowed=followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "site/profile";
    }

    @LoginRequired
    @RequestMapping(value = "/modifyPwd", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model,String checkPassword) {
        if(!checkPassword.equals(newPassword))
        {
            model.addAttribute("pwdError2", "密码不一致");
            return "site/setting";
        }
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("pwdError1", map.get("oldPasswordMsg"));
            model.addAttribute("pwdError2", map.get("newPasswordMsg"));
            return "site/setting";
        }
    }

    // 用户发布的帖子
    @RequestMapping(path = "/post/{userId}", method = RequestMethod.GET)
    public String getUserPost(@PathVariable("userId") int userId, Model model, Page page) {

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 用户信息
        model.addAttribute("user", user);

        // 帖子总数
        int postCount = discussPostService.findDiscussPostRows(userId,0);
        model.addAttribute("postCount", postCount);

        // 分页相关参数
        page.setRows(postCount);
        page.setPath("/user/post/" + userId);

        // 主语，怎样显示，是我的帖子，还是TA的帖子
        String subject = "我";
        user = hostHolder.getUser();
        if (user == null || userId != user.getId()) {
            subject = "TA";
        }
        // 小标题显示信息
        model.addAttribute("subject", subject);

        // 帖子
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),0);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 查询帖子赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        // 帖子相关信息
        model.addAttribute("discussPosts", discussPosts);

        return "site/my-post";
    }

    // 用户回复的帖子
    @RequestMapping(path = "/reply/{userId}", method = RequestMethod.GET)
    public String getUserReply(@PathVariable("userId") int userId, Model model, Page page) {

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 用户信息
        model.addAttribute("user", user);

        // 主语，怎样显示，是我的帖子，还是TA的帖子
        String subject = "我";
        user = hostHolder.getUser();
        if (user == null || userId != user.getId()) {
            subject = "TA";
        }
        // 小标题显示信息
        model.addAttribute("subject", subject);

        // 帖子总数
        int postCount = commentService.findPostCommentCountByUserId(userId, ENTITY_TYPE_POST);
        model.addAttribute("postCount", postCount);

        // 分页相关参数
        page.setRows(postCount);
        page.setPath("/user/reply/" + userId);

        //帖子及回复的相关信息
        List<ReplyPostResult> list = discussPostService.findReplyDiscussPosts(userId, page.getOffset(), page.getLimit());
        model.addAttribute("replyPost", list);

        return "site/my-reply";
    }
}
