package com.example.newcourseregistrationsystem.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseSaveDTO {
    @NotNull(message = "课程名称不能为空")
    private String courseName;

    @NotNull(message = "教师姓名不能为空")
    private String teacher;

    @NotNull(message = "最大人数不能为空")
    @Min(value = 1, message = "最大人数必须大于0")
    private Integer maxNum;

}

