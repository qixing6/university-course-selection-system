package com.example.newcourseregistrationsystem.controller;

import com.example.newcourseregistrationsystem.config.OpenApiConfig;
import com.example.newcourseregistrationsystem.config.StudentAuthInterceptor;
import com.example.newcourseregistrationsystem.dto.StudentLoginDTO;
import com.example.newcourseregistrationsystem.dto.StudentRegisterDTO;
import com.example.newcourseregistrationsystem.config.JwtProperties;
import com.example.newcourseregistrationsystem.service.StudentAuthService;
import com.example.newcourseregistrationsystem.vo.StudentLoginVO;
import com.example.newcourseregistrationsystem.vo.StudentProfileVO;
import com.example.oldcommonbase.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/student/auth")
public class StudentAuthController {

    public static final String AUTH_COOKIE_NAME = "crs_student_token";

    private final StudentAuthService studentAuthService;
    private final JwtProperties jwtProperties;

    public StudentAuthController(StudentAuthService studentAuthService, JwtProperties jwtProperties) {
        this.studentAuthService = studentAuthService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    @Operation(summary = "学生注册")
    public Result<StudentProfileVO> register(@RequestBody @Valid StudentRegisterDTO dto) {
        return Result.success(studentAuthService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "学生登录")
    public Result<StudentLoginVO> login(@RequestBody @Valid StudentLoginDTO dto, HttpServletResponse response) {
        StudentLoginVO loginVO = studentAuthService.login(dto);
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, loginVO.getToken());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) Math.max(1L, jwtProperties.getExpire() / 1000));
        response.addCookie(cookie);
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        long id = (long) request.getAttribute(StudentAuthInterceptor.ATTR_STUDENT_DB_ID);
        studentAuthService.logout(id);
        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return Result.success();
    }

    @GetMapping("/me")
    @Operation(summary = "当前登录学生信息")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    public Result<StudentProfileVO> me(HttpServletRequest request) {
        long id = (long) request.getAttribute(StudentAuthInterceptor.ATTR_STUDENT_DB_ID);
        return Result.success(studentAuthService.currentProfile(id));
    }
}
