package com.example.lianx.mapper;

import com.example.lianx.entity.LoginTicket;

import java.util.List;
import org.apache.ibatis.annotations.Param;

@Deprecated
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);

    int updateStatus(String ticket,int status);

}