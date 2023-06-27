package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sc
 * @date 2023-06-26上午 10:03
 */
@Controller
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;
    private final HostHolder hostHolder;

    @PostMapping(path = "/like")
    @ResponseBody
    @LoginRequired
    public String like(int entityType, int entityId,int entityUserId) {
        User user = hostHolder.getUser();
        likeService.like(user.getId(), entityType, entityId,entityUserId);
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("likeStatus", likeStatus);
        map.put("likeCount", likeCount);
        return CommunityUtil.getJSONString(0, null, map);
    }
}
