package com.example.lianx.mapper;

import com.example.lianx.entity.Comment;

import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    int selectPostCommentCountByUserId(int userId, int entityType);

}