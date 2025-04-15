package com.capstone1.sasscapstone1.service.RedisService;

public interface RedisService {
    void saveData(String key, Object value, long timeout);
    void updateData(String key, Object value);
    Object getData(String key);
    void deleteData(String key);

    Long getTtl(String key);
}
