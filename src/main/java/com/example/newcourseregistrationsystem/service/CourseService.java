package com.example.newcourseregistrationsystem.service;

import com.example.newcourseregistrationsystem.dto.CourseSaveDTO;
import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.vo.CourseVO;;

public interface CourseService {
    CourseVO getCourseById(Long id);

    void preloadHotCourseCache(Long... hotCourseIds);

    boolean updateCourse(Long id, CourseSaveDTO courseSaveDTO);

    Long addCourse(CourseSaveDTO courseSaveDTO);

    boolean deleteCourseById(Long id);
}
