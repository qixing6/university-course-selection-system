package com.example.newcourseregistrationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Course {
    @TableId(type= IdType.AUTO)
    private Long id;
    private String courseName;
    private String teacher;
    private Integer maxNum;
    private Integer selectedNum;
}
