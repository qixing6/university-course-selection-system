package com.example.newcourseregistrationsystem.service;

import com.example.newcourseregistrationsystem.dto.CourseSelectionDTO;
import com.example.newcourseregistrationsystem.entity.CourseSelection;
import com.example.newcourseregistrationsystem.vo.CourseSelectionVO;

public interface CourseCommandService {
      CourseSelectionVO selectCourse(CourseSelectionDTO dto);
      void dropCourse(CourseSelectionDTO dto);



}
