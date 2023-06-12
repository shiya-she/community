package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;

import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author sc
 * @date 2023-06-11下午 10:31
 */
@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController {
    private final DiscussPostService discussPostsService;
    private final HostHolder hostHolder;

    public DiscussPostController(DiscussPostService discussPostsService, HostHolder hostHolder) {
        this.discussPostsService = discussPostsService;
        this.hostHolder = hostHolder;
    }

    @PostMapping(path = {"/add"})
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostsService.addDiscussPost(post);
        //TODO 报错情况统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

}
