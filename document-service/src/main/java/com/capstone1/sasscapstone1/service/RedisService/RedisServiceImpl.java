package com.capstone1.sasscapstone1.service.RedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService{

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveData(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key,value,timeout, TimeUnit.SECONDS);
    }

    @Override
    public void updateData(String key, Object newValue) {
        redisTemplate.opsForValue().set(key, newValue);
    }

    @Override
    public Object getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
