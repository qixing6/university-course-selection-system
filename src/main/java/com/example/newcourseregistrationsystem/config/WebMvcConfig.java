package com.example.newcourseregistrationsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 配置Web MVC相关的设置，包括CORS和拦截器
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 注入学生认证拦截器，用于保护需要认证的学生相关接口
    private final StudentAuthInterceptor studentAuthInterceptor;

    public WebMvcConfig(StudentAuthInterceptor studentAuthInterceptor) {
        this.studentAuthInterceptor = studentAuthInterceptor;
    }

    // 配置跨域资源共享（CORS），允许前端应用从指定的源（如localhost:5173和localhost:3000）访问后端API，
    // 并允许特定的HTTP方法和头部，同时支持携带凭证（如Cookie）。
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:3000",
                        "http://127.0.0.1:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // 配置拦截器，添加学生认证拦截器来保护以/student/开头的接口，除了注册和登录接口，这些接口不需要认证。
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(studentAuthInterceptor)
                .addPathPatterns("/student/**")
                .excludePathPatterns(
                        "/student/auth/register",
                        "/student/auth/login");
    }
}
