package org.com.elearningservice.service;

public interface RedisService {
    public void saveData(String key, Object value, Long ttl);
    public Object getData(String key);
    public void updateData(String key, Object value);
    public void deleteData(String key);
}
