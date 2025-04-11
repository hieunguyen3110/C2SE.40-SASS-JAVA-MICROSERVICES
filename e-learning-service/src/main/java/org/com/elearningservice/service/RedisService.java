package org.com.elearningservice.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {
    public void saveData(String key, Object value, Long ttl);
    public Object getData(String key);
    public void updateData(String key, Object value, long timeout, TimeUnit timeUnit);
    public void deleteData(String key);

    boolean exists(String key);
}
