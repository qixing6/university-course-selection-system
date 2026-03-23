package com.example.newcourseregistrationsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseSelectionDTO {
    @NotBlank(message="学生ID不能为空")
    private String studentId;

    @NotBlank(message="课程ID不能为空")
    private Long courseId;
}
