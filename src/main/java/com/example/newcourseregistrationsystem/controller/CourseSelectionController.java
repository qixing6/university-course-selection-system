package com.example.newcourseregistrationsystem.controller;

import com.example.newcourseregistrationsystem.dto.CourseSelectionDTO;
import com.example.newcourseregistrationsystem.entity.CourseSelection;
import com.example.newcourseregistrationsystem.service.CourseCommandService;
import com.example.newcourseregistrationsystem.vo.CourseSelectionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.oldcommonbase.result.Result;

@CrossOrigin
@RestController
@RequestMapping("/selection")
public class CourseSelectionController {
    @Autowired
    private CourseCommandService courseCommandService;

    @PostMapping("/select")
    public Result<?> selectCourse(@RequestBody CourseSelectionDTO dto){
        CourseSelectionVO selection=courseCommandService.selectCourse(dto);
        return Result.success(selection);
    }

    @PostMapping("/drop")
    public Result<?> dropCourse(@RequestBody CourseSelectionDTO dto){
        courseCommandService.dropCourse(dto);
        return Result.success();
    }

}
