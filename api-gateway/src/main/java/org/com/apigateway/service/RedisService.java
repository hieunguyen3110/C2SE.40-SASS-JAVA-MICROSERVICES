package org.com.apigateway.service;

public interface RedisService {
    void saveData(String key, Object value, long timeout);
    Object getData(String key);
    void deleteData(String key);
}
