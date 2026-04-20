package com.example.newcourseregistrationsystem.controller;


import com.example.newcourseregistrationsystem.dto.CourseQueryDTO;
import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.service.CourseQueryService;
import com.example.newcourseregistrationsystem.vo.CourseVO;
import com.example.oldcommonbase.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/Query")
public class CourseQueryController {

    @Autowired
    private CourseQueryService courseService;

    @GetMapping("/{id}")
    public Result<CourseVO> getCourseById(@Valid CourseQueryDTO courseQueryDTO){
        CourseVO courseVO= courseService.getCourseById(courseQueryDTO.getId());
        return Result.success(courseVO);
    }

    @GetMapping("/all")
    public Result<List<CourseVO>> getAllCourses(){
        List<CourseVO> courseVOList = courseService.getAllCourses();
        return Result.success(courseVOList);
    }
}