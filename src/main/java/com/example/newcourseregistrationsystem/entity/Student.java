package com.example.newcourseregistrationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student")
public class Student {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentId;
    private String name;
    private String password;
}
