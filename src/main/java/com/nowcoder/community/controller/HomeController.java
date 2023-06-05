package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    private final UserService userService;
    private final DiscussPostService discussPostService;

    public HomeController(UserService userService, DiscussPostService discussPostService) {
        this.userService = userService;
        this.discussPostService = discussPostService;
    }

    @GetMapping(path = {"/index", "/"})
    public String getIndexPage(Model model, Page page) {
        //方法调用之前，springmvc会自动实例化Model 和 Page 并将Page注入到Model中
        //所以，在thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
//      创建一个Optional解决list空指针异常问题
        Optional<List<DiscussPost>> optional = Optional.ofNullable(list);
//      optional.map(....).ofElse(Collections.emptyList()list 不为空执行map() 结果为 List<Map<String, Object>> 否则为空的List
//      post.stream()... 对list执行流式操作 。map()  DiscussPost流 转为 HashMap流
//      .collect(Collectors.toList())将stream 流转为 list
        List<Map<String, Object>> discussPosts = optional.map(posts -> posts.stream()
                        .map(post -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("post", post);
                            map.put("user", userService.findUserById(post.getUserId()));
                            return map;
                        })
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        model.addAttribute("discussPosts", discussPosts);

        return "/index";
    }

}
