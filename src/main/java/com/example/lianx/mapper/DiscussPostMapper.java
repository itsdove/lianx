package com.example.lianx.mapper;

import com.example.lianx.entity.DiscussPost;

import java.util.List;

import org.apache.ibatis.annotations.Param;


public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int posttype);

    int selectDiscussPostRows(@Param("userId")int userId,int posttype);

    int insertDiscussPostRows(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    List<DiscussPost> searchDiscussPost(String keyword,int offset,int limit);

    int updateType(int id, int type);

    int updateStatus(int id, int status);
}