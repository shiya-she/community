package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collections;


@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    private final HostHolder holder;

    public LoginRequiredInterceptor(HostHolder holder) {
        this.holder = holder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);

        if (loginRequired == null || holder.getUser() != null) {
            return true;
        }

        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            String json = CommunityUtil.getJSONString(401, "用户未登录!", Collections.singletonMap("url", "/login"));
            writer.write(json);
            writer.flush();
            return false;
        }
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
