# 大学课程秒杀抢课系统
高校高并发选课平台 | 前后端分离架构 | 用户鉴权体系 | 全链路缓存治理 | 自研通用组件封装

## 📑 目录
1. [项目简介](#项目简介)
2. [核心功能](#核心功能)
3. [技术栈](#技术栈)
4. [自研组件说明](#自研组件说明)
5. [项目目录结构](#项目目录结构)
6. [代码封装亮点](#代码封装亮点)
## 项目简介
本项目为**高校课程抢课秒杀系统**，针对校园选课高峰期高并发流量场景，解决库存超卖、重复选课、缓存不一致、缓存穿透/击穿/雪崩等后端经典业务痛点。

项目采用自研通用组件架构，将Redis操作、分布式锁、布隆过滤器、全局异常、通用工具等底层逻辑与业务代码解耦，分层清晰、职责单一、代码可维护性强。
同时搭建完整**学生登录注册与权限鉴权体系**，基于JWT+Redis实现会话管理、权限拦截、密码安全加密；采用前后端分离架构，配套Vue前端页面，并完成阿里云服务器Docker容器化部署。

## 核心功能
| 模块 | 核心能力 |
| ---- | -------- |
| 用户权限模块 | 学生账号注册、账号密码登录、JWT身份鉴权、Redis Token缓存、登录拦截校验、BCrypt密码加密存储 |
| 课程查询模块 | 缓存穿透/击穿/雪崩全链路防护、热门课程缓存预热、全量课程数据缓存 |
| 抢课退课模块 | 分布式锁串行控制、乐观锁原子库存更新、编程式事务保证数据一致性、幂等校验防止重复选课 |
| 课程管理模块 | 课程增删改查、缓存主动更新与失效、异常兜底处理、全局统一异常捕获返回 |

## 技术栈
- 后端框架：Spring Boot、MyBatis-Plus
- 缓存与锁：Redis、Redisson分布式锁、布隆过滤器
- 自研底层组件：common-base、common-redis
- 安全鉴权：JWT、自定义拦截器、BCrypt密码加密
- 前端技术：Vue3 + Vite、Axios接口请求封装
- 其他组件：Lombok、TransactionTemplate编程式事务、MySQL
- 部署运维：Docker容器化部署、阿里云服务器

## 自研组件说明
项目核心工程化亮点为**自研通用组件封装**，抽离重复通用逻辑，实现业务层与基础设施完全解耦，业务代码仅关注自身业务逻辑。

| 组件名称 | 核心功能 | 项目作用 |
| ---- | ---- | ---- |
| common-base | 全局统一异常BusinessException、参数校验、通用工具类封装 | 统一异常抛出、日志规范，减少业务代码冗余 |
| common-redis | 封装缓存客户端CacheClient、分布式锁LockClient、布隆过滤器BloomFilterClient | 屏蔽Redis原生API细节，统一缓存、锁、过滤操作逻辑 |
通用基础组件：https://github.com/qixing6/common-base
Redis 封装组件：https://github.com/qixing6/common-redis

## 项目目录结构
### 一、后端目录（核心）
src/main/java/com/example/newcourseregistrationsystem/

├── config/ # 全局配置层

│ ├── BloomFilterWarmUp.java # 布隆过滤器预热配置

│ ├── GlobalExceptionHandler.java # 全局异常处理

│ ├── JwtProperties.java # JWT 配置

│ ├── OpenApiConfig.java # Swagger 接口文档

│ ├── PasswordEncoderConfig.java # 密码加密配置

│ ├── StudentAuthInterceptor.java # 登录权限拦截器

│ └── WebMvcConfig.java # Web 配置（拦截器注册）

├── controller/ # 接口控制层（对外提供接口）

│ ├── CourseController.java # 课程管理接口

│ ├── CourseQueryController.java # 课程查询接口

│ ├── CourseSelectionController.java # 抢课 / 退课接口

│ └── StudentAuthController.java # 登录 / 注册接口

├── dto/ # 数据传输层（请求入参）

│ ├── CourseQueryDTO.java、CourseSaveDTO.java

│ ├── CourseSelectionDTO.java

│ ├── StudentLoginDTO.java、StudentRegisterDTO.java

├── entity/ # 数据实体层（与数据库表对应）

│ ├── Course.java、CourseSelection.java、Student.java

├── mapper/ # 数据持久层（MyBatis 映射）

│ ├── CourseMapper.java、CourseQueryMapper.java

│ ├── CourseSelectionMapper.java、StudentMapper.java

├── service/ # 业务逻辑层（接口 + 实现）

│ ├── 接口层：CourseCommandService.java 等 5 个业务接口

│ └── impl/：对应业务接口的实现类

├── util/ # 工具类层

│ ├── CourseConvert.java（对象转换）、JwtUtil.java（JWT 工具）

├── vo/ # 视图返回层（接口出参）

│ ├── CourseVO.java、CourseSelectionVO.java

│ ├── StudentLoginVO.java、StudentProfileVO.java

└── 项目启动类（NewCourseRegistrationSystemApplication.java）
plaintext

### 二、前端目录（Vue3 + Vite）
frontend/
├── src/
│ ├── api/ # 接口请求封装（对接后端接口）

│ ├── router/ # 路由配置（登录 / 注册 / 选课等页面路由
）
│ ├── utils/ # 工具类（Token 管理、Axios 请求拦截）

│ ├── views/ # 页面视图（Login.vue、Register.vue、选课页面等）

│ ├── App.vue # 根组件

│ └── main.js # 入口文件

├── index.html # 入口 HTML

├── package.json # 依赖配置

└── vite.config.js # Vite 配置

plaintext

## 代码封装亮点
### 1. 底层组件解耦，业务代码纯净
封装Redis、分布式锁、布隆过滤器原生操作，业务层无需关注底层API调用，直接调用封装方法，大幅降低代码耦合。

```java
RBloomFilter<Long> bloomFilter = redisson.getBloomFilter("courseIdFilter");
String json = redis.opsForValue().get(cacheKey);
RLock lock = redisson.getLock(lockKey);
组件封装后写法：
java
运行
bloomFilterClient.mightContain("courseIdFilter", id);
CourseVO cacheJson = cacheClient.get(cacheKey, CourseVO.class);
lockClient.tryLock(lockKey, 1, 3);
2. 通用锁模板抽离，统一防死锁
封装通用加锁执行方法，抢课、退课复用逻辑，规范加锁解锁流程，避免锁泄露与死锁问题。
java
运行
private <T> T executeWithLock(String lockKey, Supplier<T> action) {
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
3. 标准化缓存与熔断兜底
统一缓存查询流程、异常捕获、Redis 熔断降级，无需重复编写异常处理代码，提升接口稳定性。
高并发核心解决方案
1. 缓存三大问题完整治理
表格
缓存问题	解决方案
缓存穿透	布隆过滤器拦截无效 ID + 空值缓存
缓存击穿	分布式互斥锁 + 双重校验
缓存雪崩	Key 随机过期时间 + Redis 异常熔断降级
2. 防超卖完整架构
课程级分布式锁保证请求串行执行
数据库乐观锁原子更新库存，避免 ABA 问题
编程式事务保证选课记录插入与库存更新原子一致
studentId + courseId 联合唯一索引兜底，杜绝重复选课
3. 缓存一致性策略
热门课程启动自动缓存预热
课程增删改后主动失效对应缓存
合理设置过期时间，平衡性能与数据一致性
压测性能
核心选课接口压测 990 QPS 稳定运行
无库存超卖、无锁异常、无脏数据问题
有效降低高并发场景下数据库访问压力
