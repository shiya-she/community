package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;

import java.io.IOException;
import java.util.Optional;

@Controller
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

    public UserController(UserService userService, HostHolder hostHolder) {
        this.userService = userService;
        this.hostHolder = hostHolder;
    }

    @GetMapping(path = "/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

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
        File file = new File(uploadPath  + filename);
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


}


