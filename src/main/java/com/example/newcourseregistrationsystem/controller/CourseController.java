package com.example.newcourseregistrationsystem.controller;

import com.example.newcourseregistrationsystem.dto.CourseQueryDTO;
import com.example.newcourseregistrationsystem.dto.CourseSaveDTO;
import com.example.newcourseregistrationsystem.service.CourseService;

import com.example.newcourseregistrationsystem.vo.CourseVO;
import com.example.oldcommonbase.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping("/{id}")
    public Result<CourseVO> getCourseById(@Valid CourseQueryDTO courseQueryDTO){
        CourseVO courseVO= courseService.getCourseById(courseQueryDTO.getId());
        return Result.success(courseVO);
    }

    @PostMapping("/preload")
    public Result<String> preloadHotCourseCache(){
        courseService.preloadHotCourseCache(1L, 2L, 3L);
        return Result.success("热点课程缓存预热完成");
    }

    @PutMapping("/{id}")
    public Result<String> updateCourse(
            @PathVariable Long id,
            @RequestBody @Valid CourseSaveDTO courseSaveDTO
    ){
        boolean success = courseService.updateCourse(id,courseSaveDTO);
        return success ? Result.success("课程更新成功") : Result.fail("课程更新失败");
    }

    @PostMapping("/add")
    public Result<String> addCourse(@RequestBody @Valid CourseSaveDTO courseSaveDTO){
        Long newId=courseService.addCourse(courseSaveDTO);
        return Result.success("课程新增成功,ID:"+newId);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCourse(@Valid CourseQueryDTO courseQueryDTO){
        boolean success=courseService.deleteCourseById(courseQueryDTO.getId());
        return success ? Result.success("课程删除成功") : Result.fail("课程删除失败");
    }


}
