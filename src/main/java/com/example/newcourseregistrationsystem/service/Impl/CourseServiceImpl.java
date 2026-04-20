package com.example.newcourseregistrationsystem.service.Impl;
import com.example.commonredis.api.CacheClient;
import com.example.newcourseregistrationsystem.dto.CourseSaveDTO;
import com.example.newcourseregistrationsystem.util.CourseConvert;
import com.example.newcourseregistrationsystem.vo.CourseVO;
import com.example.oldcommonbase.exception.BusinessException;
import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.mapper.CourseMapper;
import com.example.newcourseregistrationsystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;



@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {
 private static final Logger log=LoggerFactory.getLogger(CourseServiceImpl.class);
   private final CacheClient cacheClient;
   private final CourseMapper courseMapper;
   private static final String COURSE_CACHE_KEY_PREFIX="course:";
    private final CourseConvert courseConvert;

    @Override
    public CourseVO getCourseById(Long id){
           String cacheKey=COURSE_CACHE_KEY_PREFIX+id;

           Course course=cacheClient.get(cacheKey,Course.class);
                if(course!=null){
                log.info("缓存击中，ID为{}",id);
                return courseConvert.toVO(course);
              }
              course=courseMapper.selectById(id);
                if(course==null){
                 log.warn("缓存未命中，DB查询不到该课程，ID为{}",id);
                  throw new BusinessException("课程不存在");
                }
                cacheClient.setWithExpire(cacheKey,course,60);
                log.info("缓存更新，ID为{}",id);
                return courseConvert.toVO(course);
    }

    @Override
    public void preloadHotCourseCache(Long... hotCourseIds){
     for(Long id:hotCourseIds){
      try{
      Course course=courseMapper.selectById(id);
       if(course==null){
        log.warn("预热缓存失败，DB查询不到课程，ID为{}",id);
        continue;
       }
        String cacheKey=COURSE_CACHE_KEY_PREFIX+id;
        cacheClient.setWithExpire(cacheKey,course,60);
        log.info("预热缓存成功，ID为{}",id);
       }catch (Exception e){
        log.error("预热缓存失败，ID为{}",id,e);
      }
     }
    }

    @Override
    public boolean updateCourse(Long id, CourseSaveDTO courseSaveDTO){
        Course course=courseMapper.selectById(id);
        if(course==null){
            log.warn("更新课程失败，课程信息为空");
            throw new BusinessException("课程信息不能为空");
        }
        Course updatedCourse=courseConvert.toEntity(courseSaveDTO);
        updatedCourse.setId(id);
        int rows=courseMapper.updateById(updatedCourse);
        if(rows==0){
           log.warn("更新课程失败，DB未找到该课程，ID为{}",id);
            throw new BusinessException("课程不存在");
        }
        String cacheKey=COURSE_CACHE_KEY_PREFIX+id;
        try {
            cacheClient.delete(cacheKey);
            log.info("删除缓存成功，ID为{}",id);
        }catch (Exception e){
            log.error("删除缓存失败，ID为{},异常为{}",course.getId(),e.getMessage(),e);
        }
        log.info("更新课程成功，ID为{}",course.getId());
        return true;
    }

    @Override
    public Long addCourse(CourseSaveDTO courseSaveDTO){
        Course course=courseConvert.toEntity(courseSaveDTO);
        if(course==null){
            log.warn("添加课程失败，课程信息为空");
            throw new BusinessException("课程信息不能为空");
        }
        int rows=courseMapper.insert(course);
        if(rows==0){
            log.error("添加课程失败，DB插入失败，课程信息：{}",course);
            throw new BusinessException("添加课程失败");
        }
        log.info("添加课程成功，ID为{}",course.getId());
        return course.getId();
    }

    @Override
    public boolean deleteCourseById(Long id){
        int rows=courseMapper.deleteById(id);
        if(rows==0){
            log.warn("删除课程失败，DB未找到该课程，ID为{}",id);
            throw new BusinessException("课程不存在");
        }
        String cacheKey=COURSE_CACHE_KEY_PREFIX+id;
        try {
            cacheClient.delete(cacheKey);
            log.info("删除缓存成功，ID为{}",id);
        }catch (Exception e){
            log.error("删除缓存失败，ID为{}",id,e);
    }
        log.info("删除课程成功，ID为{}",id);
        return true;
    }
}
