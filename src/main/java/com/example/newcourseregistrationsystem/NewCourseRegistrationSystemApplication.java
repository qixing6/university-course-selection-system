package com.example.newcourseregistrationsystem;

import com.example.newcourseregistrationsystem.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.example.newcourseregistrationsystem", "com.example.commonredis"})
@MapperScan("com.example.newcourseregistrationsystem.mapper")
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class NewCourseRegistrationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewCourseRegistrationSystemApplication.class, args);
    }

}
