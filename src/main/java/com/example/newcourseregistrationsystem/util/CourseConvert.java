package com.example.newcourseregistrationsystem.util;

import com.example.newcourseregistrationsystem.dto.CourseSaveDTO;
import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.entity.CourseSelection;
import com.example.newcourseregistrationsystem.vo.CourseSelectionVO;
import com.example.newcourseregistrationsystem.vo.CourseVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component // 交给Spring管理，和之前@Mapper用法完全一样
public class CourseConvert {

    // DTO 转 实体类
    public Course toEntity(CourseSaveDTO dto) {
        if (dto == null) {
            return null;
        }
        Course course = new Course();
        course.setCourseName(dto.getCourseName());
        course.setTeacher(dto.getTeacher());
        course.setMaxNum(dto.getMaxNum());
        course.setSelectedNum(0); // 新增默认0
        return course;
    }

    // 实体 转 VO
    public CourseVO toVO(Course course) {
        if (course == null) {
            return null;
        }
        CourseVO vo = new CourseVO();
        // 一一对应赋值，绝对不会错位！
        vo.setId(course.getId());
        vo.setCourseName(course.getCourseName());
        vo.setTeacher(course.getTeacher());
        vo.setMaxNum(course.getMaxNum());
        vo.setSelectedNum(course.getSelectedNum());
        return vo;
    }

    public CourseSelectionVO toVOSelection(CourseSelection courseSelection){
        if(courseSelection==null){
            return null;
        }
        CourseSelectionVO vo=new CourseSelectionVO();
        vo.setId(courseSelection.getId());
        vo.setStudentId(courseSelection.getStudentId());
        vo.setCourseId(courseSelection.getCourseId());
        vo.setCreateTime(courseSelection.getCreateTime());
        return vo;
    }
    // 列表转换
    public List<CourseVO> toVOList(List<Course> courseList) {
        List<CourseVO> voList = new ArrayList<>();
        if (courseList == null || courseList.isEmpty()) {
            return voList;
        }
        for (Course course : courseList) {
            voList.add(toVO(course));
        }
        return voList;
    }
}