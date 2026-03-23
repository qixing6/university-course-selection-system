# 大学抢课秒杀系统
> 高并发场景下的课程抢课/退课/管理系统，基于自研组件封装实现缓存优化、超卖控制，代码结构整洁可维护

## 📋 项目简介
本项目为高校课程抢课秒杀核心系统，针对抢课高峰期高并发场景，解决**缓存三大问题（穿透/击穿/雪崩）**、**课程库存超卖**、**重复选课**等核心痛点；通过自研通用组件封装，实现业务代码与底层缓存/锁逻辑解耦，大幅提升代码整洁度和可维护性。

## ✨ 核心功能
| 模块         | 核心能力                                                                 |
|--------------|--------------------------------------------------------------------------|
| 课程查询     | 缓存穿透/击穿/雪崩防护、热门课程缓存预热、全量课程缓存                     |
| 课程抢课/退课 | 分布式锁防超卖、乐观锁更新库存、事务保证数据一致性、幂等性校验             |
| 课程管理     | 课程增删改查、缓存更新/删除、异常兜底                                     |

## 🛠 技术栈
- 核心框架：Spring Boot、MyBatis-Plus
- 缓存/锁：Redis、Redisson（分布式锁/布隆过滤器）
- 自研组件：common-base（通用异常/工具）、common-redis（缓存/锁/布隆过滤器客户端）
- 其他：Lombok（简化代码）、TransactionTemplate（编程式事务）

## 📦 自研组件依赖
本项目核心优化点在于基于两个自制通用组件（轮子）封装业务逻辑，彻底解决代码耦合问题：
| 组件名称       | 核心功能                                                                 | 作用                                                                 |
|----------------|--------------------------------------------------------------------------|----------------------------------------------------------------------|
| common-base    | 通用异常（BusinessException）、参数校验、工具类封装                       | 统一异常处理，减少业务代码中的重复校验/异常抛出逻辑                   |
| common-redis   | CacheClient（缓存操作）、LockClient（分布式锁）、BloomFilterClient（布隆过滤器） | 封装Redis底层操作，业务层仅关注核心逻辑，无需关心Redis API调用细节     |

## 📂 核心代码结构
```
src/main/java/com/example/newcourseregistrationsystem/
├── service/Impl/
│   ├── CourseQueryServiceImpl.java  # 课程查询（缓存三大问题解决方案）
│   ├── CourseCommandServiceImpl.java # 抢课/退课（分布式锁+乐观锁+事务）
│   └── CourseServiceImpl.java       # 课程管理（增删改查+缓存同步）
├── entity/          # 数据库实体（Course/CourseSelection）
├── vo/              # 视图对象（CourseVO）
├── dto/             # 数据传输对象（CourseSelectionDTO/CourseSaveDTO）
├── mapper/          # 数据库映射层
└── util/            # 实体转换（CourseConvert）
```

## 🔥 核心优化亮点（代码整洁性体现）
### 1. 组件封装解耦，业务逻辑聚焦
通过`common-redis`组件封装Redis/锁/布隆过滤器底层操作，业务代码无需关注具体API调用，仅聚焦核心逻辑：
```java
// 优化前：直接操作Redisson/RedisTemplate，代码耦合
RBloomFilter<Long> bloomFilter = redisson.getBloomFilter("courseIdFilter");
String json = redis.opsForValue().get(cacheKey);
RLock lock = redisson.getLock(lockKey);

// 优化后：调用封装的客户端，代码简洁
bloomFilterClient.mightContain("courseIdFilter", id);
CourseVO cacheJson = cacheClient.get(cacheKey, CourseVO.class);
lockClient.tryLock(lockKey, 1, 3);
```

### 2. 职责单一，方法分层清晰
将参数校验、锁操作、事务执行等通用逻辑抽离为独立工具方法，业务方法仅保留核心逻辑：
```java
// 通用锁执行方法（抽离后，抢课/退课复用）
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

// 抢课核心逻辑（仅关注业务，无需关心锁的细节）
@Override
public CourseSelection selectCourse(CourseSelectionDTO dto) {
    validate(dto);
    String lockKey = "course:select:" + dto.getCourseId();
    return executeWithLock(lockKey, () -> transactionTemplate.execute(status -> {
        // 核心抢课逻辑...
    }));
}
```

### 3. 缓存逻辑标准化，异常处理统一
基于`CacheClient`封装缓存操作，统一异常捕获和日志输出，避免重复代码：
```java
// 课程查询缓存逻辑（标准化）
@Override
public CourseVO getCourseById(Long id) {
    if(id==null||id<=0){
        throw new BusinessException(404,"非法的课程ID");
    }
    String cacheKey = COURSE_CACHE_KEY_PREFIX + id;
    
    // 1. 布隆过滤器防穿透（一行调用，无需关心底层实现）
    if (!bloomFilterClient.mightContain("courseIdFilter", id)) {
        throw new BusinessException(404,"课程信息不存在,id:"+id);
    }
    
    // 2. Redis异常熔断（统一捕获，无需重复try-catch）
    try {
        CourseVO CacheJson = cacheClient.get(cacheKey, CourseVO.class);
        if (CacheJson != null) return CacheJson;
    } catch (RedisConnectionFailureException e) {
        log.error("Redis访问异常，直接熔断", e);
        throw new BusinessException(503, "系统繁忙，请稍后再试");
    }
    
    // 后续逻辑...
}
```

## 🚀 核心特性实现
### 1. 缓存三大问题解决方案
| 问题         | 实现方案                                                                 |
|--------------|--------------------------------------------------------------------------|
| 缓存穿透     | BloomFilterClient拦截无效课程ID + CacheClient空值缓存                    |
| 缓存击穿     | LockClient分布式锁 + 双重缓存检查                                        |
| 缓存雪崩     | CacheClient随机过期时间 + Redis连接异常熔断                              |

### 2. 防超卖核心逻辑
- 分布式锁（课程级）保证同一课程抢课串行执行；
- 乐观锁+原子SQL更新库存（`selected_num=selected_num+1`），避免ABA问题；
- 编程式事务保证选课记录插入和库存更新原子性；
- 唯一索引（studentId+courseId）兜底防重复选课。

### 3. 缓存管理最佳实践
- 热门课程主动预热缓存，减少缓存未命中；
- 课程增删改查后同步删除/更新缓存，避免数据不一致；
- 全量课程缓存缩短过期时间，平衡性能与一致性。

## 📝 代码规范与整洁性总结
1. **组件化封装**：基于`common-base`/`common-redis`封装通用逻辑，业务代码仅关注核心场景；
2. **职责单一**：每个方法仅做一件事（如`executeWithLock`仅处理锁，`validate`仅做参数校验）；
3. **异常统一**：基于`common-base`的`BusinessException`统一异常抛出，前端友好提示；
4. **日志规范**：分级日志（info/debug/warn/error），关键操作记录上下文（课程ID/学生ID）；
5. **命名语义化**：变量/方法命名清晰（如`lockAcquired`/`executeWithLock`），无需额外注释即可理解。

## 📄 许可证
MIT License

## 📞 联系方式
- GitHub：qixing6
- 核心组件仓库：
  - common-base：https://github.com/qixing6/common-base
  - common-redis：https://github.com/qixing6/common-redis
