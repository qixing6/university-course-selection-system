package com.example.newcourseregistrationsystem.service;

import com.example.newcourseregistrationsystem.dto.CourseSelectionDTO;
import com.example.newcourseregistrationsystem.entity.CourseSelection;

public interface CourseCommandService {
      CourseSelection selectCourse(CourseSelectionDTO dto);
      void dropCourse(CourseSelectionDTO dto);



}
