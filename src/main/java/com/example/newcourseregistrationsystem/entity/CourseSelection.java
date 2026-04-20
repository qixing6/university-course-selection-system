package com.example.newcourseregistrationsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSelection {
    private Long id;
    private String studentId;
    private Long courseId;
    private LocalDateTime createTime;
}

