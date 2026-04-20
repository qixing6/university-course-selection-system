package com.example.newcourseregistrationsystem.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    // secret：JWT签名的密钥，应该足够复杂和随机，以防止被猜测或暴力破解。可以使用UUID或其他随机字符串生成工具来创建一个强密钥。
    private String secret = "change_me";
    // expire：JWT的过期时间，单位为毫秒。这里默认设置为24小时（86,400,000毫秒）。
    // 根据实际需求，可以调整这个值，例如设置为1小时（3,600,000毫秒）或更长时间。
    private long expire = 86_400_000L;
    // tokenHeader：HTTP请求头中携带JWT的字段名称，通常使用"Authorization"。在前端发送请求时，需要将JWT放在这个字段中
    private String tokenHeader = "Authorization";
    // tokenPrefix：JWT在HTTP请求头中的前缀，通常使用"Bearer "。在前端发送请求时，需要将JWT放在这个前缀后面，例如"Authorization
    private String tokenPrefix = "Bearer ";
    // redisTokenKeyPrefix：在Redis中存储JWT的键前缀，通常使用"student:auth:token:"。
    // 在保存和验证JWT时，会将学生数据库ID附加到这个前缀后面，形成完整的Redis键，例如"student:auth:token:123"。
    private String redisTokenKeyPrefix = "student:auth:token:";

}
