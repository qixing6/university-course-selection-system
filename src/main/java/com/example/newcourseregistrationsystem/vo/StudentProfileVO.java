package com.example.newcourseregistrationsystem.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentProfileVO {

    private Long id;
    private String studentId;
    private String name;
}
