package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoginTicketInterceptor loginTicketInterceptor;

    public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*。jpg",
                        "/**/*.jpeg");
    }
}
