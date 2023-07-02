package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstants;
import com.nowcoder.community.util.CommunityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Map;


@Controller
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;
    private final Producer kaptchaProducer;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @GetMapping(path = "/register")
    public String getRegisteredPage() {
        return "/site/register";
    }

    @GetMapping(path = "/login")
    public String getLoginPage() {
        return "/site/login";
    }


    @PostMapping(path = "/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAllAttributes(map);
            return "/site/register";
        }
    }

    @GetMapping(path = "/activation/{userId}/{code}")
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == CommunityConstants.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == CommunityConstants.ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经成功激活了！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //将验证码存入session
        session.setAttribute("kaptcha", text);
        //将图片输出给浏览器
        response.setContentType("image/jpeg");

        try (OutputStream os = response.getOutputStream()) {
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error(String.format("响应验证码失败:%s", e.getMessage()));
        }


    }

    @PostMapping(path = "/login")
    public String login(User user, String code, boolean rememberMe,
                        Model model, HttpSession session, HttpServletResponse response) {
        String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) ||
                StringUtils.isBlank(code) ||
                !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds = rememberMe ? CommunityConstants.REMEMBER_EXPIRED_SECONDS : CommunityConstants.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(user, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAllAttributes(map);

            return "/site/login";
        }
    }

    @LoginRequired
    @GetMapping(path = "/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }

    @GetMapping(path = "/forget")
    public String getForgetPage() {
        return "/site/forget";

    }

    @GetMapping(path = "/forgetCode/{email}")
    @ResponseBody
    public String getCode(@PathVariable("email") String email) {
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return CommunityUtil.getJSONString(404, "未找到" + email + "用户");
        }
        String code = kaptchaProducer.createText();
        userService.forgetCode(user.getId(), email, code, CommunityConstants.USER_FORGET);
        return CommunityUtil.getJSONString(200);
    }


    @PostMapping(path = "/forget")
    public String forget(String email, String code, String password, Model model) {
        Map<String, Object> map = userService.resetPassword(email, code, password);
        if (map.size() > 0) {
            model.addAllAttributes(map);
            return "/site/forget";
        }
        model.addAttribute("msg", "你的账号" + email + "密码已经修改完毕！请登录！");
        model.addAttribute("target", "/login");
        return "/site/operate-result";
    }

}
