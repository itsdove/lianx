package com.example.lianx.mapper;

import com.example.lianx.entity.DiscussPost;

import java.util.List;

import org.apache.ibatis.annotations.Param;


public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    int selectDiscussPostRows(@Param("userId")int userId);

    int insertDiscussPostRows(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);
}