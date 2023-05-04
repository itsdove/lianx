package com.example.lianx.mapper;

import com.example.lianx.entity.User;

import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    void insertUser(User user);

    void updateStatus(int userId, int i);

    int updateHeader(int userId, String headerUrl);

    void updatePassword(int userId, String password);
}