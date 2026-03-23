package com.example.newcourseregistrationsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSelectionVO {
    private Long id;
    private String studentId;
    private Long courseId;
    private LocalDateTime createTime;
}
