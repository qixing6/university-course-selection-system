package com.example.newcourseregistrationsystem.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.newcourseregistrationsystem.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseQueryMapper extends BaseMapper<Course> {
    @Select("SELECT id FROM course ")
    List<Long> selectAllids();
}
