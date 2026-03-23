package com.example.newcourseregistrationsystem.vo;

import lombok.Data;

@Data
public class CourseVO {
    private Long id;
    private String courseName;
    private String teacher;
    private Integer maxNum;
    private Integer selectedNum;
}
