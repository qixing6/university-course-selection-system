package com.example.newcourseregistrationsystem.util;

import com.example.newcourseregistrationsystem.dto.CourseSaveDTO;
import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.vo.CourseVO;
import jakarta.validation.constraints.NotBlank;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseConvert {
    //DTO转实体类
    // 新增课程时，选课人数默认为0
    Course toEntity(CourseSaveDTO courseSaveDTO);

    //实体类转VO
    @NotBlank
    CourseVO toVO(Course course);

    List<CourseVO> toVOList(List<Course> courseList);
}
