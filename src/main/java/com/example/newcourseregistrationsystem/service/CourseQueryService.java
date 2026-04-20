package com.example.newcourseregistrationsystem.service;

import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.vo.CourseVO;

import java.util.List;

public interface CourseQueryService {
        /**
        * 根据课程ID查询课程信息
        * @param id 课程ID
        * @return 课程信息
        */
        CourseVO getCourseById(Long id);

    /**
     * 查询所有课程信息
     * @return 课程信息列表
     */
    List<CourseVO> getAllCourses();
}
