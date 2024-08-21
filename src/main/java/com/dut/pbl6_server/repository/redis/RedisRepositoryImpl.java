package com.dut.pbl6_server.repository.redis;

import com.dut.pbl6_server.common.util.CommonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@Log4j2
public class RedisRepositoryImpl implements RedisRepository {
    public final RedisTemplate<String, Object> redisTemplate;
    public final HashOperations<String, String, Object> hashOperations;
    private final String DEBUG_PREFIX = "REDIS ==> ";

    @Autowired
    public RedisRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void save(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.info("%s Saved key: %s value: %s".formatted(DEBUG_PREFIX, key, value));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void expire(String key, long timeToLiveInDay) {
        try {
            redisTemplate.expire(key, timeToLiveInDay, TimeUnit.DAYS);
            log.info("%s Set expiration for key: %s in %s days".formatted(DEBUG_PREFIX, key, String.valueOf(timeToLiveInDay)));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void save(String key, String hashKey, Object value) {
        try {
            // Check value is a java class
            Class<?> clazz = value.getClass();
            var isSimpleValueType = ClassUtils.isSimpleValueType(clazz);
            var isPrimitiveOrWrapper = ClassUtils.isPrimitiveOrWrapper(clazz);
            var isPrimitiveArray = ClassUtils.isPrimitiveArray(clazz) || ClassUtils.isPrimitiveWrapperArray(clazz);
            var isCollection = Collection.class.isAssignableFrom(clazz);
            if (isSimpleValueType || isPrimitiveOrWrapper || isPrimitiveArray || isCollection) {
                hashOperations.put(key, hashKey, value);
                log.info("%s Saved key: %s hashKey: %s value: %s".formatted(DEBUG_PREFIX, key, hashKey, value));
            } else {
                var jsonValue = CommonUtils.Json.encode(value);
                if (jsonValue != null) {
                    hashOperations.put(key, hashKey, jsonValue);
                    log.info("%s Saved key: %s hashKey: %s value: %s".formatted(DEBUG_PREFIX, key, hashKey, jsonValue));
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean hashExist(String key, String hashKey) {
        try {
            log.info("%s Check if hashKey: %s exists in key: %s".formatted(DEBUG_PREFIX, hashKey, key));
            return hashOperations.hasKey(key, hashKey);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public Object findByKey(String key) {
        try {
            log.info("%s Find value by key: %s".formatted(DEBUG_PREFIX, key));
            return redisTemplate.opsForValue().get(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public Map<String, Object> findHashFieldByKey(String key) {
        try {
            log.info("%s Find all hash fields by key: %s".formatted(DEBUG_PREFIX, key));
            return hashOperations.entries(key);
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    @Override
    public Object findByHashKey(String key, String hashKey) {
        try {
            log.info("%s Find value by key: %s hashKey: %s".formatted(DEBUG_PREFIX, key, hashKey));
            return hashOperations.get(key, hashKey);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public List<Object> findAllByHashKeyPrefix(String key, String hashKeyPrefix) {
        try {
            log.info("%s Find all values by key: %s hashKeyPrefix: %s".formatted(DEBUG_PREFIX, key, hashKeyPrefix));
            return hashOperations.entries(key)
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(hashKeyPrefix))
                .map(Map.Entry::getValue)
                .toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    @Override
    public Set<String> getFieldPrefixes(String key) {
        try {
            log.info("%s Get all field prefixes by key: %s".formatted(DEBUG_PREFIX, key));
            return hashOperations.entries(key).keySet();
        } catch (Exception ignored) {
            return Set.of();
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.info("%s Delete key: %s".formatted(DEBUG_PREFIX, key));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void delete(String key, String hashKey) {
        try {
            hashOperations.delete(key, hashKey);
            log.info("%s Delete key: %s hashKey: %s".formatted(DEBUG_PREFIX, key, hashKey));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void delete(String key, List<String> hashKeys) {
        try {
            hashOperations.delete(key, hashKeys.toArray());
            log.info("%s Delete key: %s hashKeys: %s".formatted(DEBUG_PREFIX, key, String.valueOf(hashKeys)));
        } catch (Exception ignored) {
        }
    }
}
