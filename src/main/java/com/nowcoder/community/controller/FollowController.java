package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstants;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author sc
 * @date 2023-06-28下午 06:07
 */
@Controller
@RequiredArgsConstructor
public class FollowController {
    private final HostHolder hostHolder;
    private final FollowService followService;
    private final UserService userService;

    @PostMapping(path = "/follow")
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注！");
    }


    @PostMapping(path = "/unfollow")
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    @GetMapping(path = "/followees/{userId}")
    public String getFollowees(@PathVariable int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("/followee/" + userId + "不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, CommunityConstants.ENTITY_TYPE_USER));
        List<Map<String, Object>> userList =
                followService.findFollowees(userId, page.getOffset(), page.getLimit());
        userList.forEach(
                map -> {
                    User user1 = (User) map.get("user");
                    map.put("hasFollowed", hasFollowed(user1.getId()));
                }
        );
        model.addAttribute("users", userList);
        return "/site/followee";
    }

    @GetMapping(path = "/followers/{userId}")
    public String getFollowers(@PathVariable int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("/followee/" + userId + "不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(CommunityConstants.ENTITY_TYPE_USER, userId));
        List<Map<String, Object>> userList =
                followService.findFollowers(userId, page.getOffset(), page.getLimit());
        userList.forEach(
                map -> {
                    User user1 = (User) map.get("user");
                    map.put("hasFollowed", hasFollowed(user1.getId()));
                }
        );
        model.addAttribute("users", userList);
        return "/site/follower";
    }

    public boolean hasFollowed(int userId) {
        return Optional.ofNullable(hostHolder.getUser())
                .map(user ->
                        followService.hasFollowed(user.getId(), CommunityConstants.ENTITY_TYPE_USER, userId)
                ).orElse(false);
    }


}

