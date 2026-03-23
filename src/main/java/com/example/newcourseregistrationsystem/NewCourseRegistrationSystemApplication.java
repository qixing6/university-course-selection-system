package com.example.newcourseregistrationsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.example.newcourseregistrationsystem", "com.example.commonredis"})
@MapperScan("com.example.newcourseregistrationsystem.mapper")
@SpringBootApplication
public class NewCourseRegistrationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewCourseRegistrationSystemApplication.class, args);
    }

}
