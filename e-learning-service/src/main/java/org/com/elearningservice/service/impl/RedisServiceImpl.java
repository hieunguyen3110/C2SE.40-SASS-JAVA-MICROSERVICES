package org.com.elearningservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.elearningservice.service.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveData(String key, Object value, Long timeout) {
        redisTemplate.opsForValue().set(key,value,timeout, TimeUnit.SECONDS);
    }

    @Override
    public void updateData(String key, Object newValue, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, newValue, timeout, timeUnit);
    }

    @Override
    public Object getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }
}
