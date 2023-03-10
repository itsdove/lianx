package com.example.lianx.controller;

import com.example.lianx.entity.User;
import com.example.lianx.service.UserService;
import com.example.lianx.util.CommunityConstant;
import com.example.lianx.util.CommunityUtil;
import com.example.lianx.util.RedisKeyUtil;
import com.example.lianx.util.SpringSecurityUtil;
import com.google.code.kaptcha.Producer;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path="/register",method= RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path="/login",method= RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }



    @RequestMapping(path="/register",method= RequestMethod.POST)
    public String register(Model model , User user){
        Map<String ,Object> map=userService.register(user);
        if(map==null||map.isEmpty()){
            model.addAttribute("msg","??????????????????????????????");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path="/activation/{userId}/{code}",method=RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId,@PathVariable("code") String code){
        int result=userService.activation(userId,code);
        if(result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","????????????");
            model.addAttribute("target","/login");
        }else if(result==ACTIVATION_REPEAT){
            model.addAttribute("msg","???????????????????????????");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","?????????????????????????????????");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path="/kaptcha",method= RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response){
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);

        String kaptchaOwner= CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("contextPath");
        response.addCookie(cookie);
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);

        response.setContentType("image/png");

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping(path="/login",method= RequestMethod.POST)
    public String login(String username ,String password,String code,boolean rememberme,
                        Model model,HttpServletResponse httpServletResponse,@CookieValue("kaptchaOwner")String kaptchaOwner) {

    String kaptcha=null;
    if(StringUtils.isNotBlank(kaptchaOwner)){
        String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        kaptcha= (String) redisTemplate.opsForValue().get(redisKey);
    }

    if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
        model.addAttribute("codeMsg","??????????????????");
        return "/site/login";
    }

    int expiredSeconds=rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
    Map<String,Object> map=userService.login(username,password,expiredSeconds);
    if(map.containsKey("ticket")){
        Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
        cookie.setPath(contextPath);
        cookie.setMaxAge(expiredSeconds);
        httpServletResponse.addCookie(cookie);

        List<String> list=new ArrayList<>();
        list.add("user");

        if(SpringSecurityUtil.isLogin()){
            // ????????????????????????????????????
            System.out.println(SpringSecurityUtil.getCurrentUsername());
        }else{
            // ??????????????????????????????
            SpringSecurityUtil.login(username,password,list);
        }

        List<GrantedAuthority> authorities=new ArrayList<>();
        authorities.add((GrantedAuthority) () -> "user");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, authorities);
        SecurityContextHolder.setContext(new SecurityContextImpl(token));

        return "redirect:/index";
    }else{
        model.addAttribute("usernameMsg",map.get("usernameMsg"));
        model.addAttribute("passwordMsg",map.get("passwordMsg"));
        return "/site/login";
    }
    }

    @RequestMapping(path="/logout",method = RequestMethod.GET)
    public  String logout(@CookieValue("ticket")String ticket){
        jakarta.servlet.http.HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        request.getSession().removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}
