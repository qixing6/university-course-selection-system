package com.example.newcourseregistrationsystem.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentLoginVO {

    private String token;
    private long expireAt;
    private StudentProfileVO student;


}
