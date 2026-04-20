package com.example.newcourseregistrationsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT = "bearer-jwt";

    // 配置OpenAPI文档，添加JWT认证的安全方案
    @Bean
    public OpenAPI studentAuthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("课程注册系统 API")
                        .description("学生认证使用 Header：`Authorization: Bearer <token>`")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
