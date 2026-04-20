package com.example.newcourseregistrationsystem.service.Impl;

import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.mapper.CourseQueryMapper;
import com.example.newcourseregistrationsystem.service.CourseQueryService;
import com.example.newcourseregistrationsystem.util.CourseConvert;
import com.example.newcourseregistrationsystem.vo.CourseVO;
import com.example.oldcommonbase.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import com.example.commonredis.api.BloomFilterClient;
import com.example.commonredis.api.CacheClient;
import com.example.commonredis.api.LockClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class CourseQueryServiceImpl implements CourseQueryService {
    private static final Logger log= LoggerFactory.getLogger(CourseQueryServiceImpl.class);
    private static final String COURSE_CACHE_KEY_PREFIX = "course:";
    private static final String ALL_COURSES_CACHE_KEY = "course:all";
    private final CourseQueryMapper courseQueryMapper;
    private final CacheClient cacheClient;
    private final BloomFilterClient bloomFilterClient;
    private final LockClient lockClient;
    private final CourseConvert courseConvert;


    @Override
    public CourseVO getCourseById(Long id) {
        if(id==null||id<=0){
            throw new BusinessException(404,"课程ID不合法");
        }

        String cacheKey = COURSE_CACHE_KEY_PREFIX + id;
        // ========== 1. 缓存穿透防御：布隆过滤器 ==========
        if (!bloomFilterClient.mightContain("courseIdFilter", id)) {
            log.info("课程ID{}不存在于布隆过滤器，拦截穿透请求", id);
            throw new BusinessException(404,"课程不存在");
        }

        // ========== 2. 缓存雪崩防御：Redis宕机时降级 ==========
        try {
            CourseVO CacheJson = cacheClient.get(cacheKey, CourseVO.class);
            if (CacheJson != null) {
                log.info("缓存击中，ID为{}", id);
                return CacheJson;
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis访问异常，直接熔断", e);
            throw new BusinessException(503, "系统繁忙，请稍后再试");
        }

        // ========== 3. 缓存击穿防御：分布式锁 ==========
        String lockKey = "course:" + id;
        CourseVO courseVO;
        boolean lockAcquired=false;
        try {
            // 尝试获取锁：最多等1秒，锁自动释放3秒（避免死锁）
           lockAcquired = lockClient.tryLock(lockKey, 1, 3);
            if (!lockAcquired) {
                Thread.sleep(50);
                CourseVO retryCache=cacheClient.get(cacheKey, CourseVO.class);
                if(retryCache!=null){
                    return retryCache;
                }
                throw new BusinessException(503,"系统繁忙，请稍后再试");
            }
            // 双重检查缓存（防止锁等待期间缓存已写入）
            CourseVO courseJson = cacheClient.get(cacheKey, CourseVO.class);
            if (courseJson != null) {
                log.info("双重检查缓存击中，ID为{}", id);
                return courseJson;
            }

            // ========== 4. 查数据库 ==========
            log.info("缓存未命中，查询数据库，ID为{}", id);
            Course course = courseQueryMapper.selectById(id);

            // ========== 5. 缓存雪崩防御：过期时间加随机值 + 空值缓存 ==========
            courseVO = courseConvert.toVO(course);
            if (courseVO != null) {
                cacheClient.setWithRandomExpire(cacheKey, courseVO, 30, 5);// 基础过期30分钟，随机±5分钟
                log.info("课程ID{}查询成功，写入缓存", id);
            } else {
                cacheClient.setNullValue(cacheKey, 10);// 空值缓存10分钟
                log.debug("课程ID{}不存在，写入空值缓存", id);
                throw new BusinessException(404, "课程不存在");
            }
            return courseVO;
        } catch (InterruptedException e) {
            log.error("分布式锁异常,id:{}", id);
            Thread.currentThread().interrupt(); // 恢复中断状态
            throw new BusinessException(503, "系统繁忙，请稍后再试");
        } finally {
            if (lockAcquired) {
                lockClient.unlock(lockKey);
                log.debug("释放锁成功，ID为{}", id);
            }
        }
    }




    @Override
    public List<CourseVO> getAllCourses() {
            try{
                List<CourseVO> course=cacheClient.getAll(ALL_COURSES_CACHE_KEY, CourseVO.class);
                if(course!=null&&!course.isEmpty()){
                    log.info("查询所有课程，缓存命中");
                    return course;
                }

            List<Course> courseList=courseQueryMapper.selectList(null);
                List<CourseVO> courseVOList = courseConvert.toVOList(courseList);
            if(courseVOList!=null&&!courseVOList.isEmpty()) {
                cacheClient.setWithRandomExpire(ALL_COURSES_CACHE_KEY, courseVOList, 10, 5);
                log.debug("查询所有课程，数据库查询成功，写入缓存，课程数量:{}", courseVOList.size());
            }
            return courseVOList;
            }catch(RedisConnectionFailureException e){
                log.error("Redis连接失败，直接熔断", e);
                throw new BusinessException(503, "系统繁忙，请稍后再试");
            }
    }
}
