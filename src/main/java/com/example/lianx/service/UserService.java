package com.example.lianx.service;

import com.example.lianx.entity.LoginTicket;
import com.example.lianx.entity.User;
import com.example.lianx.mapper.UserMapper;
import com.example.lianx.util.CommunityConstant;
import com.example.lianx.util.CommunityUtil;
import com.example.lianx.util.MailClient;
import com.example.lianx.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id){
        User user = getCache(id);
        if(user==null){
            user=initCache(id);
        }
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map=new HashMap<>();

        if(user==null){
            throw new IllegalArgumentException("空参");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("usernameMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("usernameMsg","邮箱不能为空");
            return map;
        }
        User u=userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","账号已经存在");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowconder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        Context context=new Context();
        context.setVariable("email",user.getEmail());
        String url=domain +contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活账号",content);
        return map;
    }

    public int activation(int userId,String code){
        User user=userMapper.selectById(userId);
        if(user.getStatus()==1){
            return  ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return  ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login (String username,String password,int expired){
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        User user=userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","用户不存在");
            return map;
        }

        if(user.getStatus()==0){
            map.put("usernameMsg","用户未激活");
            return map;
        }

        password=CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password))
        {
            map.put("passwordMsg","密码不正确");
            return  map;
        }

        LoginTicket loginTicket=new LoginTicket();
        map.put("type",user.getType());
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expired*1000));

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return  map;

    }

    public void logout(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);

    }

    public LoginTicket findLogingTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId,String headerUrl){
        clearCache(userId);
        return userMapper.updateHeader(userId,headerUrl);
    }

    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    private User getCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    public User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    private void clearCache(int userId){
        String redisKey=RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
