package com.example.newcourseregistrationsystem.config;

import com.example.newcourseregistrationsystem.service.StudentTokenRedisService;
import com.example.newcourseregistrationsystem.util.JwtUtil;
import com.example.newcourseregistrationsystem.controller.StudentAuthController;
import com.example.oldcommonbase.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

// 学生认证拦截器，负责验证学生的JWT令牌是否合法有效，并将学生信息存储在请求属性中供后续处理使用。
@Component
public class StudentAuthInterceptor implements HandlerInterceptor {

    // 定义两个常量，分别用于存储学生数据库ID和学号在请求属性中的键名。这些属性可以在后续的控制器或服务中通过request.getAttribute()方法获取。
    public static final String ATTR_STUDENT_DB_ID = "studentDbId";
    public static final String ATTR_STUDENT_NO = "studentNo";

    private final JwtProperties jwtProperties;
    private final JwtUtil jwtUtil;
    private final StudentTokenRedisService studentTokenRedisService;
    private final ObjectMapper objectMapper;

    public StudentAuthInterceptor(
            JwtProperties jwtProperties,
            JwtUtil jwtUtil,
            StudentTokenRedisService studentTokenRedisService,
            ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.jwtUtil = jwtUtil;
        this.studentTokenRedisService = studentTokenRedisService;
        this.objectMapper = objectMapper;
    }

    @Override
    // request：当前HTTP请求对象，包含请求的所有信息，如URL、方法、头部、参数等。
    // response：当前HTTP响应对象，可以用来设置响应状态码、头部和响应体等。
    // handler：处理请求的处理器对象，通常是一个控制器方法的映射对象，可以用来获取处理器的相关信息。
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = extractToken(request);
        if (token.isEmpty()) {
            return unauthorized(response, "缺少认证令牌");
        }
        try {
            // 解析JWT令牌，验证其有效性和过期性，并从令牌中提取学生数据库ID和学号。
            // Claims是JWT中的一个接口，表示JWT的声明部分，包含了JWT的各种信息，如主题、发行者、过期时间等。在这里，我们通过调用claims.getSubject()方法获取学生数据库ID
            Claims claims = jwtUtil.parse(token);
            long studentDbId = Long.parseLong(claims.getSubject());
            if (!studentTokenRedisService.matchesStoredToken(studentDbId, token)) {
                return unauthorized(response, "登录已失效，请重新登录");
            }
            // 将学生数据库ID和学号存储在请求属性中，并允许请求继续处理；如果验证失败，则返回401未授权响应，并提供相应的错误消息。
            String studentNo = claims.get(JwtUtil.CLAIM_STUDENT_NO, String.class);
            request.setAttribute(ATTR_STUDENT_DB_ID, studentDbId);
            request.setAttribute(ATTR_STUDENT_NO, studentNo);
            return true;
        } catch (ExpiredJwtException e) {
            return unauthorized(response, "登录已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(response, "令牌无效");
        }
    }

    // 从HTTP请求中提取JWT令牌。它首先尝试从请求头中获取令牌，如果找到了，则去掉前缀并返回；
    // 如果没有找到，则继续检查Cookie中是否存在名为"auth_token"的Cookie，并返回其值；如果仍然没有找到，则返回空字符串。
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getTokenHeader());
        if (header != null && !header.isBlank()) {
            String prefix = jwtProperties.getTokenPrefix().trim();
            if (!header.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return "";
            }
            return header.substring(prefix.length()).trim();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }
        for (Cookie cookie : cookies) {
            if (StudentAuthController.AUTH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue() == null ? "" : cookie.getValue().trim();
            }
        }
        return "";
    }

    // 返回401未授权响应，并提供相应的错误消息。它设置响应状态码为401，响应内容类型为JSON，并使用ObjectMapper将一个包含错误消息的Result对象写入响应体中。
    private boolean unauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.fail(message));
        return false;
    }
}
