package com.example.newcourseregistrationsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentLoginDTO {

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^[0-9]{6,10}$", message = "学号须为6-10位数字")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度须为6-32位")
    private String password;
}
