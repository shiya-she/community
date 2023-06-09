package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoginTicketInterceptor loginTicketInterceptor;
    @Value("${community.path.upload}")
    private String uploadPath;
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
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/user/header/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
