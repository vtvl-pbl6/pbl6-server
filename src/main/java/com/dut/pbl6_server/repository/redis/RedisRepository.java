package com.dut.pbl6_server.repository.redis;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface is used to define the methods that are used to interact with Redis.
 */
@NoRepositoryBean
public interface RedisRepository {
    void expire(String key, long timeToLiveInDay);

    void save(String key, String value);

    void save(String key, String hashKey, Object value);

    boolean hashExist(String key, String hashKey);

    Object findByKey(String key);

    Object findByHashKey(String key, String hashKey);

    Map<String, Object> findHashFieldByKey(String key);

    List<Object> findAllByHashKeyPrefix(String key, String hashKeyPrefix);

    Set<String> getFieldPrefixes(String key);

    void delete(String key);

    void delete(String key, String hashKey);

    void delete(String key, List<String> hashKeys);
}
