package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;

import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstants;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import java.util.stream.Collectors;



/**
 * @author sc
 * @date 2023-06-11下午 10:31
 */
@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController {
    private final DiscussPostService discussPostsService;
    private final HostHolder hostHolder;
    private final UserService userService;

    private final CommentService commentService;

    public DiscussPostController(DiscussPostService discussPostsService, HostHolder hostHolder, UserService userService, CommentService commentService) {
        this.discussPostsService = discussPostsService;
        this.hostHolder = hostHolder;
        this.userService = userService;
        this.commentService = commentService;
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
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @GetMapping(path = "/detail/{discussPostId}")
    public String getDiscussionPost(@PathVariable("discussPostId") int discussionPostId, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostsService.findDiscussPostById(discussionPostId);
        model.addAttribute("post", post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussionPostId);
        page.setRows(post.getCommentCount());
        //评论:给帖子的评论
        //回复:给评论的评论
        //评论列表
        List<Comment> comments = commentService.findCommentsByEntity(CommunityConstants.ENTITY_TYPE_POST, discussionPostId, page.getOffset(), page.getLimit());
        comments = Optional.ofNullable(comments).orElse(Collections.emptyList());

        List<Map<String, Object>> maps = comments.stream().map(
                comment -> {
                    Map<String, Object> outMap = new HashMap<>();
                    outMap.put("comment", comment);
                    outMap.put("user", userService.findUserById(comment.getUserId()));
                    List<Comment> replyList = Optional.ofNullable(
                                    commentService.findCommentsByEntity(CommunityConstants.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE)
                            )
                            .orElse(Collections.emptyList());
                    List<Map<String, Object>> replyMap = replyList.stream().map(
                            reply -> {
                                Map<String, Object> inMap = new HashMap<>();
                                inMap.put("reply", reply);
                                inMap.put("user", userService.findUserById(reply.getUserId()));
                                User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                                inMap.put("target", target);
                                return inMap;
                            }
                    ).collect(Collectors.toList());
                    outMap.put("reply",replyMap);
                    int replyCount = commentService.findCommentCount(CommunityConstants.ENTITY_TYPE_COMMENT, comment.getId());
                    outMap.put("replyCount", replyCount);
                    return outMap;

                }
        ).collect(Collectors.toList());

        model.addAttribute("comments", maps);
        return "/site/discuss-detail";
    }
}
