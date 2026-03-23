package com.example.newcourseregistrationsystem.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.commonredis.api.LockClient;
import com.example.newcourseregistrationsystem.dto.CourseSelectionDTO;
import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.entity.CourseSelection;
import com.example.newcourseregistrationsystem.mapper.CourseMapper;
import com.example.newcourseregistrationsystem.mapper.CourseSelectionMapper;
import com.example.newcourseregistrationsystem.service.CourseCommandService;
import com.example.oldcommonbase.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
public class CourseCommandServiceImpl implements CourseCommandService {

    private static final Logger log = LoggerFactory.getLogger(CourseCommandServiceImpl.class);

    private final CourseSelectionMapper courseSelectionMapper;
    private final LockClient lockClient;
    private final TransactionTemplate transactionTemplate;
    private final CourseMapper courseMapper;

    // ========== 选课 ==========

    @Override
    public CourseSelection selectCourse(CourseSelectionDTO dto) {
        validate(dto);
        String studentId = dto.getStudentId();
        Long courseId = dto.getCourseId();

        // 锁粒度：课程级（防超卖）+ 学生课程级（防重复提交）→ 简化：只用课程级，唯一索引防重复
        String lockKey = "course:select:" + courseId;

        return executeWithLock(lockKey, () -> transactionTemplate.execute(status -> {
            //先给这门课加分布式锁，锁成功后，开启数据库事务，在事务内执行对数据库的操作，最后返回收课结果
            //return将锁+事务+选课最终得到的结果返回
            // 1. 查课程（FOR UPDATE或乐观锁，这里用乐观锁）
            Course course = courseMapper.selectById(courseId);
            if (course == null) throw new BusinessException("课程不存在");
            if (course.getMaxNum() <= course.getSelectedNum()) {
                throw new BusinessException("课程名额已满");
            }

            // 2. 幂等：唯一索引(studentId, courseId)兜底，先查后插减少冲突
            if (courseSelectionMapper.exists(new LambdaQueryWrapper<CourseSelection>()
                    .eq(CourseSelection::getStudentId, studentId)
                    .eq(CourseSelection::getCourseId, courseId))) {
                throw new BusinessException("已选择此课程");
            }

            // 3. 插入选课记录
            CourseSelection selection = new CourseSelection();
            selection.setStudentId(studentId);
            selection.setCourseId(courseId);
            selection.setCreateTime(LocalDateTime.now());
            courseSelectionMapper.insert(selection);

            // 4. 【关键】乐观锁更新库存：WHERE selected_num = 当前值
//            int affected = courseMapper.update(null, new LambdaUpdateWrapper<Course>()
//                    .eq(Course::getId, courseId)
//                    .eq(Course::getSelectedNum, course.getSelectedNum())  // 乐观锁条件
//                    .set(Course::getSelectedNum, course.getSelectedNum() + 1));  // 新值
            /**
             *新方法：原子性SQL
             * 1.无BAB问题，不受中间状态干扰
             * 2.性能更优：1次SQL搞定
             */
            int affected= courseMapper.update(null, Wrappers.<Course>lambdaUpdate()
                    .eq(Course::getId,courseId)
                    .lt(Course::getSelectedNum,course.getMaxNum())
                    .setSql("selected_num=selected_num+1")
            );


            if (affected == 0) {
                throw new BusinessException("选课失败，课程已满或不存在");
            }

            log.info("学生{}选课{}成功", studentId, courseId);
            return selection;
        }));
    }

    // ========== 退课 ==========

    @Override
    public void dropCourse(CourseSelectionDTO dto) {
        validate(dto);
        String studentId = dto.getStudentId();
        Long courseId = dto.getCourseId();

        String lockKey = "course:drop:" + courseId;  // 同课程串行，防止并发减到负数

        executeWithLock(lockKey, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                // 1. 查课程
                Course course = courseMapper.selectById(courseId);
                if (course == null) throw new BusinessException("课程不存在");

                // 2. 查是否已选
                boolean exists = courseSelectionMapper.exists(new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
                        .eq(CourseSelection::getCourseId, courseId));
                if (!exists) throw new BusinessException("未选择此课程");

                // 3. 删除选课记录
                int deleted = courseSelectionMapper.delete(new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
                        .eq(CourseSelection::getCourseId, courseId));
                if (deleted == 0) throw new BusinessException("退课失败，请重试");

                // 4. 【关键】乐观锁减库存：防止并发减到负数
                if (course.getSelectedNum() > 0) {
                    int affected = courseMapper.update(null,Wrappers.<Course>lambdaUpdate()
                            .eq(Course::getId,courseId)
                            .gt(Course::getSelectedNum,0)
                            .setSql("selected_num=selected_num-1")
                    );

                    if (affected == 0) {
                        throw new BusinessException("退课失败，课程不存在或已无选课记录");
                    }
                }
            });
            return null;
        });
    }

    // ========== 工具方法 ==========

    private void validate(CourseSelectionDTO dto) {
        if (dto == null || dto.getCourseId() == null || dto.getCourseId() <= 0
                || dto.getStudentId() == null || dto.getStudentId().isBlank()) {
            throw new BusinessException(400, "学生ID和课程ID无效");
        }
    }

    private <T> T executeWithLock(String lockKey, Supplier<T> action) {
        //Supplier<T> action:要执行的业务逻辑，用Lambda传入，最后返回一个结果T
        if (!lockClient.tryLock(lockKey, 3, 10)) {
            throw new BusinessException("操作频繁，请稍后再试");
        }
        try {
            return action.get();
        } finally {
            if (lockClient.isHeldByCurrentThread(lockKey)) {
                lockClient.unlock(lockKey);
            }
        }
    }
}