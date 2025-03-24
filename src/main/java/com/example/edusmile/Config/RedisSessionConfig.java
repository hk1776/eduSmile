package com.example.edusmile.Config;


import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 4800)              //redis config
public class RedisSessionConfig {
        // redis 세션 저장 별도 설정 x"spring:session:sessions:163503e0-f8aa-4464-a399-8c0c1eafaaa3"
}
