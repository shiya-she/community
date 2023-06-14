package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.util.HostHolder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author sc
 * @date 2023-06-14下午 08:19
 */

@Controller
@RequestMapping(path = "/comment")
public class CommentController {
    private final CommentService commentService;

    private final HostHolder hostHolder;

    public CommentController(CommentService commentService, HostHolder hostHolder) {
        this.commentService = commentService;
        this.hostHolder = hostHolder;
    }

    @PostMapping(path = "/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussionPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        return "redirect:/discuss/detail/" + discussionPostId;
    }
}
