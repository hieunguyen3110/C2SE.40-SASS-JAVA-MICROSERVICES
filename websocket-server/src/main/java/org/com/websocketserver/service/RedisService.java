package org.com.websocketserver.service;

public interface RedisService {
    void saveData(String key, Object value, long timeout);
    void updateData(String key, Object value);
    Object getData(String key);
    void deleteData(String key);
}
