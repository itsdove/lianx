package com.example.lianx.controller.interceptor;

import com.example.lianx.entity.User;
import com.example.lianx.service.MessageService;
import com.example.lianx.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
       User user=hostHolder.getUser();
       if(user!=null&&modelAndView!=null){
           int letterUnreadCount=messageService.findLetterUnreadCount(user.getId(), null);
           int noticeUnreadCount=messageService.findNoticeUnreadCount(user.getId(),null);
           modelAndView.addObject("allUnreadCount",letterUnreadCount+noticeUnreadCount);
       }
    }
}
