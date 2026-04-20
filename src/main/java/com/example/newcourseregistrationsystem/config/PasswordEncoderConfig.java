package com.example.newcourseregistrationsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    // 定义一个PasswordEncoder Bean，使用BCrypt算法进行密码加密。BCrypt是一种强哈希函数，适合用于存储密码，可以有效防止暴力破解和字典攻击。
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
