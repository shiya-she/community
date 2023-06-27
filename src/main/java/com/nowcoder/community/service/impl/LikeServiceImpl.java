package com.nowcoder.community.service.impl;

import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author sc
 * @date 2023-06-26上午 09:32
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        //        if (Boolean.TRUE.equals(isMember)) {
        //            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        //        } else {
        //            redisTemplate.opsForSet().add(entityLikeKey, userId);
        //        }
        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean member = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();
                if (Boolean.TRUE.equals(member)) {
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }

                return redisOperations.exec();
            }
        });

    }

    //查询某实体点赞数量
    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return Optional.ofNullable(redisTemplate.opsForSet().size(entityLikeKey))
                .orElse(0L);
    }

    //查询某用户对实体的点赞状态
    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(entityLikeKey, userId)) ? 1 : 0;
    }

    //查询某用户获得的赞
    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }

}
