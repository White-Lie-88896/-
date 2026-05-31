package com.sky.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void testRedisConnection() {
        // 1. 存入一个键值对
        redisTemplate.opsForValue().set("sky:test", "hello_redis");

        // 2. 取出并验证
        String value = redisTemplate.opsForValue().get("sky:test");
        System.out.println("从 Redis 获取的值: " + value);

        // 3. 使用断言确保值正确
        assertEquals("hello_redis", value);

        // 4. 删除键
        redisTemplate.delete("sky:test");
        System.out.println("测试键已删除");
    }
}
