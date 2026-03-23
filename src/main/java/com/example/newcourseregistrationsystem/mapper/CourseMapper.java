package com.example.newcourseregistrationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newcourseregistrationsystem.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
