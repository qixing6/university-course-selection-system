package com.example.newcourseregistrationsystem.util;

import com.example.newcourseregistrationsystem.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtUtil {

    // 定义一个常量CLAIM_STUDENT_NO，表示JWT令牌中存储学生学号的声明键名。在生成和解析JWT令牌时，可以使用这个常量来统一访问学生学号的声明。
    public static final String CLAIM_STUDENT_NO = "studentNo";

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // 生成用于签名JWT令牌的密钥。首先，使用SHA-256算法对JWT密钥进行哈希处理，得到一个固定长度的字节数组。
    // 然后，使用这个字节数组创建一个HMAC-SHA密钥对象，用于签名JWT令牌。这样做可以增强密钥的安全性，避免直接使用原始密钥字符串。
    private SecretKey signingKey() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    // 创建JWT令牌的方法，接受学生数据库ID和学号作为参数。首先，获取当前时间，并计算令牌的过期时间（当前时间加上配置的过期时长）。
    // 然后，使用Jwts.builder()创建一个JWT构建器，设置令牌的主题为学生数据库ID，并添加一个自定义声明来存储学生学号。
    // 最后，设置令牌的签名算法和密钥，并生成最终的JWT字符串。
    public String createToken(long studentDbId, String studentNo) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtProperties.getExpire());
        return Jwts.builder()
                .subject(String.valueOf(studentDbId))
                .claim(CLAIM_STUDENT_NO, studentNo)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())
                .compact();
    }


    // 解析JWT令牌的方法，接受一个JWT字符串作为参数。使用Jwts.parser()创建一个JWT解析器，设置用于验证签名的密钥。
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 获取JWT令牌的过期时间的方法，直接从配置属性中返回过期时长（单位为毫秒）。这个方法可以在生成令牌时使用，以确保令牌的有效期符合预期。
    public long getExpireMillis() {
        return jwtProperties.getExpire();
    }
}
