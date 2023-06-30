package com.nowcoder.community.service.impl;

import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstants;
import com.nowcoder.community.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sc
 * @date 2023-06-27下午 10:24
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final RedisTemplate redisTemplate;
    private final UserService userService;

    @Override
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {

            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                redisOperations.multi();
                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询关注实体的数量
     */
    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询实体的粉丝数量
     */
    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否已经关注实体
     */
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * 查询某个用户关注的人
     */
    @Override
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, CommunityConstants.ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        List<Map<String, Object>> list = Optional.ofNullable(targetIds)
                .stream()
                .flatMap(Set::stream)
                .map(id -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("user", userService.findUserById(id));
                    Double score = redisTemplate.opsForZSet().score(followeeKey, id);
                    map.put("followTime", new Date(score.longValue()));
                    return map;
                })
                .collect(Collectors.toList());
        return list;
    }

    /**
     * 查询某个用户的粉丝
     */
    @Override
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(3, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        List<Map<String, Object>> list = Optional.ofNullable(targetIds)
                .stream()
                .flatMap(Set::stream)
                .map(id -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("user", userService.findUserById(id));
                    Double score = redisTemplate.opsForZSet().score(followerKey, id);
                    map.put("followTime", new Date(score.longValue()));
                    return map;
                })
                .collect(Collectors.toList());
        return list;
    }
}
