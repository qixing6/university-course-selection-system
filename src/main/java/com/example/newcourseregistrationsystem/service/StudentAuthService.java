package com.example.newcourseregistrationsystem.service;

import com.example.newcourseregistrationsystem.dto.StudentLoginDTO;
import com.example.newcourseregistrationsystem.dto.StudentRegisterDTO;
import com.example.newcourseregistrationsystem.vo.StudentLoginVO;
import com.example.newcourseregistrationsystem.vo.StudentProfileVO;

public interface StudentAuthService {

    //注册
    StudentProfileVO register(StudentRegisterDTO dto);

    //登录
    StudentLoginVO login(StudentLoginDTO dto);

    //登出
    void logout(long studentDbId);

    //获取当前登录学生的个人信息
    StudentProfileVO currentProfile(long studentDbId);
}
