-- 学生表（登录注册）。若表已存在且结构一致可跳过。
-- 数据库名与 application.yaml 中 spring.datasource.url 一致（示例为 first）。

USE `first`;

CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `student_id` VARCHAR(32) NOT NULL COMMENT '学号，登录账号',
    `name` VARCHAR(64) NOT NULL COMMENT '姓名',
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密后的密码',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生';
