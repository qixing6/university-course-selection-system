package com.example.newcourseregistrationsystem.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.newcourseregistrationsystem.dto.StudentLoginDTO;
import com.example.newcourseregistrationsystem.dto.StudentRegisterDTO;
import com.example.newcourseregistrationsystem.entity.Student;
import com.example.newcourseregistrationsystem.mapper.StudentMapper;
import com.example.newcourseregistrationsystem.service.StudentAuthService;
import com.example.newcourseregistrationsystem.service.StudentTokenRedisService;
import com.example.newcourseregistrationsystem.util.JwtUtil;
import com.example.newcourseregistrationsystem.vo.StudentLoginVO;
import com.example.newcourseregistrationsystem.vo.StudentProfileVO;
import com.example.oldcommonbase.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentAuthServiceImpl implements StudentAuthService {

    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StudentTokenRedisService studentTokenRedisService;

    public StudentAuthServiceImpl(
            StudentMapper studentMapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            StudentTokenRedisService studentTokenRedisService) {
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.studentTokenRedisService = studentTokenRedisService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)// 注册和登录都需要事务，确保数据一致性,rollbackFor = Exception.class表示遇到任何异常都回滚事务
    public StudentProfileVO register(StudentRegisterDTO dto) {
        //等效于select count(*) from student where student_id = ?
        Long count = studentMapper.selectCount(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentId, dto.getStudentId()));

        if (count != null && count > 0) {
            throw new BusinessException(409, "学号已被注册");
        }
        Student entity = new Student();
        entity.setStudentId(dto.getStudentId());
        //trim()去掉前后空格，防止用户输入时不小心加了空格导致注册成功但登录失败
        entity.setName(dto.getName().trim());
        // 密码加密存储
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        studentMapper.insert(entity);
        return toProfile(entity);
    }

    @Override
    public StudentLoginVO login(StudentLoginDTO dto) {
        //等价于select * from student where student_id = ? limit 1
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentId, dto.getStudentId()));
        if (student == null) {
            throw new BusinessException(401, "学号或密码错误");
        }
        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), student.getPassword())) {
            throw new BusinessException(401, "学号或密码错误");
        }
        // 生成 JWT token，包含学生数据库ID和学号
        String token = jwtUtil.createToken(student.getId(), student.getStudentId());
        // 将 token 存储到 Redis，设置过期时间与 JWT 一致
        studentTokenRedisService.saveToken(student.getId(), token);
        // 计算过期时间戳，单位为毫秒
        long expireAt = System.currentTimeMillis() + jwtUtil.getExpireMillis();
        // 返回登录信息，包括 token、过期时间和学生基本信息
        return StudentLoginVO.builder()
                .token(token)
                .expireAt(expireAt)
                .student(toProfile(student))
                .build();
    }

    @Override
    public void logout(long studentDbId) {
        studentTokenRedisService.removeToken(studentDbId);
    }

    @Override
    public StudentProfileVO currentProfile(long studentDbId) {
        Student student = studentMapper.selectById(studentDbId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        return toProfile(student);
    }

    private static StudentProfileVO toProfile(Student student) {
        return StudentProfileVO.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .name(student.getName())
                .build();
    }
}
