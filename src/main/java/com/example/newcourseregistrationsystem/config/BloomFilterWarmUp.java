package com.example.newcourseregistrationsystem.config;
import com.example.commonredis.api.BloomFilterClient;
import com.example.newcourseregistrationsystem.mapper.CourseQueryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Component
public class BloomFilterWarmUp implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterWarmUp.class);

    @Autowired
    private CourseQueryMapper courseQueryMapper;

    @Autowired
    private BloomFilterClient bloomFilterClient;

    @Override
    public void run(ApplicationArguments args) {
        // 启动时一次性加载所有课程ID
        List<Long> ids = courseQueryMapper.selectAllids(); // 只查ID列
        bloomFilterClient.initFilter("courseIdFilter", ids.size() * 1000L, 0.01);
        ids.forEach(id -> bloomFilterClient.put("courseIdFilter", id));
        log.info("布隆过滤器预热完成，加载{}个课程ID", ids.size());
    }
}