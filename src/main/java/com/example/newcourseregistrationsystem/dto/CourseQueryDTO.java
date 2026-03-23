package com.example.newcourseregistrationsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseQueryDTO {
    @NotNull(message = "数据ID不能为空!")
    private Long id;
}
