package com.nowcoder.community.service;

import com.nowcoder.community.entity.Comment;

import java.util.List;

/**
 * @author sc
 * @date 2023-06-13上午 12:09
 */
public interface CommentService {
    List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int findCommentCount(int entityType, int entityId);
}
