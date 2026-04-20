package com.example.newcourseregistrationsystem.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.commonredis.api.LockClient;
import com.example.commonredis.impl.RedisCacheClientImpl;
import com.example.newcourseregistrationsystem.dto.CourseSelectionDTO;
import com.example.newcourseregistrationsystem.entity.Course;
import com.example.newcourseregistrationsystem.entity.CourseSelection;
import com.example.newcourseregistrationsystem.entity.Student;
import com.example.newcourseregistrationsystem.mapper.CourseMapper;
import com.example.newcourseregistrationsystem.mapper.CourseSelectionMapper;
import com.example.newcourseregistrationsystem.mapper.StudentMapper;
import com.example.newcourseregistrationsystem.service.CourseCommandService;
import com.example.newcourseregistrationsystem.util.CourseConvert;
import com.example.newcourseregistrationsystem.vo.CourseSelectionVO;
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
    private final StudentMapper studentMapper;
    private final LockClient lockClient;
    private final TransactionTemplate transactionTemplate;
    private final CourseMapper courseMapper;
    private final RedisCacheClientImpl cacheClient;
    private final CourseConvert courseConvert;


    // ========== 选课 ==========

    @Override
    public CourseSelectionVO selectCourse(CourseSelectionDTO dto) {
        validate(dto);
        // ==========================
        // 【1. 学生ID/课程ID 合法性校验】
        // ==========================
        String studentId = dto.getStudentId();
        Long courseId = dto.getCourseId();

        // 1.1 非空校验
        if (studentId == null || studentId.isBlank()) {
            throw new BusinessException("学生ID不能为空");
        }
        if (courseId == null || courseId <= 0) {
            throw new BusinessException("课程ID不合法");
        }

// 1.2 学生ID格式校验
        if (!studentId.matches("^\\d{6,10}$")) {
            throw new BusinessException("学生ID格式错误，必须为6-10位纯数字");
        }

// 1.3 根据学号查询学生（正确写法）
        QueryWrapper<Student> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId);
        Student student = studentMapper.selectOne(qw);

        if (student == null) {
            throw new BusinessException("学生不存在");
        }




        // 锁粒度：课程级（防超卖）+ 学生课程级（防重复提交）→ 简化：只用课程级，唯一索引防重复
        String lockKey = "course:select:" + courseId;

        return lockClient.executeWithLock(lockKey, () -> transactionTemplate.execute(status -> {
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

            /**
             *新方法：原子性SQL
             * 1.无BAB问题，不受中间状态干扰
             * 2.性能更优：1次SQL搞定
             * 3.affected表示有多少行被更新
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
            cacheClient.delete("course:all");
            cacheClient.delete("course:"+courseId);
            return courseConvert.toVOSelection(selection);
        }));
    }

    // ========== 退课 ==========

    @Override
    public void dropCourse(CourseSelectionDTO dto) {
        validate(dto);
        // ==========================
        // 【1. 学生ID/课程ID 合法性校验】
        // ==========================
        String studentId = dto.getStudentId();
        Long courseId = dto.getCourseId();

        // 1.1 非空校验
        if (studentId == null || studentId.isBlank()) {
            throw new BusinessException("学生ID不能为空");
        }
        if (courseId == null || courseId <= 0) {
            throw new BusinessException("课程ID不合法");
        }

// 1.2 学生ID格式校验
        if (!studentId.matches("^\\d{6,10}$")) {
            throw new BusinessException("学生ID格式错误，必须为6-10位纯数字");
        }

// 1.3 根据学号查询学生（正确写法）
        QueryWrapper<Student> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId);
        Student student = studentMapper.selectOne(qw);

        if (student == null) {
            throw new BusinessException("学生不存在");
        }


        String lockKey = "course:drop:" + courseId;  // 同课程串行，防止并发减到负数

        lockClient.executeWithLock(lockKey, () -> {
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
                    cacheClient.delete("course:all");
                    cacheClient.delete("course:"+courseId);
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

}