package com.nowcoder.community.service;

/**
 * @author sc
 * @date 2023-06-26上午 09:33
 */
public interface LikeService {
    void like(int userId, int entityType, int entityId);

    //查询某实体点赞数量
    long     findEntityLikeCount(int entityType, int entityId);

    //查询某用户对实体的点赞状态
    int findEntityLikeStatus(int userId, int entityType, int entityId);
}
