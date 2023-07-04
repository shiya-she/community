package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstants;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final UserService userService;
    private final HostHolder hostHolder;
    private final LikeService likeService;
    private final FollowService followService;
    private final DiscussPostService discussPostService;
    private final CommentService commentService;

    @LoginRequired
    @GetMapping(path = "/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping(path = "/upload")
    public String uploadHandler(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = Optional.ofNullable(filename)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf('.'))).orElse("");
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File file = new File(uploadPath + filename);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error(String.format("上传文件失败%s", e.getMessage()));
            throw new RuntimeException("上传文件失败，服务器异常！", e);
        }
        //更新当前用户的头像的路径(web访问路径)
        //http://localhost:8080/community/user/header/xxxx.xxx
        User user = hostHolder.getUser();
        String header = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), header);
        return "redirect:/index";
    }

    @LoginRequired
    @PostMapping(path = "/changePassword")
    public String changePassword(String oldPassword, String newPassword, Model model) {
        if (StringUtils.equals(oldPassword, newPassword)) {
            model.addAttribute("newPasswordMsg", "新密码不能与旧密码相同！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if (user == null) {
            model.addAttribute("msg", "您还未登录！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }
        Map<String, Object> map = userService.updatePassword(oldPassword, newPassword, user);
        if (!map.isEmpty()) {
            model.addAllAttributes(map);
            return "/site/setting";
        }
        return "redirect:/logout";
    }

    //个人主页
    @GetMapping(path = "/profile/{userId}")
    public String profile(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("userId不存在！");
        }
        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(user.getId());
        model.addAttribute("likeCount", likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(user.getId(), CommunityConstants.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        long followerCount = followService.findFollowerCount(CommunityConstants.ENTITY_TYPE_USER, user.getId());
        model.addAttribute("followerCount", followerCount);
        Optional<User> userOptional = Optional.ofNullable(hostHolder.getUser());
        boolean hasFollowed = userOptional
                .map(user1 -> followService.hasFollowed(user1.getId(), CommunityConstants.ENTITY_TYPE_USER, userId))
                .orElse(false);
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

    @GetMapping(path = "/my-post")
    @LoginRequired
    public String myPost(Model model, Page page) {
        User user = hostHolder.getUser();
        int rows = discussPostService.findDiscussPostRows(user.getId());
        page.setLimit(10);
        page.setPath("/user/my-post");
        page.setRows(rows);
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> maps = Optional.ofNullable(discussPosts)
                .stream()
                .flatMap(List::stream)
                .map(post -> {
                    Map<String, Object> map = new HashMap<>();
                    long likeCount = likeService.findEntityLikeCount(CommunityConstants.ENTITY_TYPE_POST, post.getId());
                    map.put("post", post);
                    map.put("likeCount", likeCount);
                    return map;
                })
                .collect(Collectors.toList());
        model.addAttribute("rows", rows);
        model.addAttribute("discussPosts", maps);
        return "/site/my-post";
    }

    @GetMapping(path = "/my-reply")
    @LoginRequired
    public String myReply(Model model, Page page) {
        User user = hostHolder.getUser();
        int rows = commentService.findCommentCountByUserId(CommunityConstants.ENTITY_TYPE_POST,user.getId());
        page.setLimit(10);
        page.setPath("/user/my-reply");
        page.setRows(rows);
        List<Comment> comments = commentService.findCommentsByUserId(CommunityConstants.ENTITY_TYPE_POST, user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> maps = Optional.ofNullable(comments)
                .stream()
                .flatMap(List::stream)
                .map(comment -> {
                    Map<String, Object> map = new HashMap<>();
                    DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                    map.put("postId", post.getId());
                    map.put("postTitle", post.getTitle());
                    map.put("comment", comment);
                    return map;
                })
                .collect(Collectors.toList());
        model.addAttribute("rows",rows);
        model.addAttribute("comments",maps);
        return "/site/my-reply";
    }
}


