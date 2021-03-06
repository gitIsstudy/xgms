package com.huel.xgms.base.service.impl;

import com.huel.xgms.base.service.IRedisOpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis 常用操作服务实现
 * @author wsq
 * @date 2018/3/11
 */
@Service
public class RedisOpServiceImpl implements IRedisOpService {

    private static Logger LOG = LoggerFactory.getLogger(RedisOpServiceImpl.class);
    private RedisTemplate redisTemplate;
    private ValueOperations valueOperations;
    private HashOperations hashOperations;

    @Autowired
    public RedisOpServiceImpl(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public String get(String key) {
        Object o = valueOperations.get(key);
        LOG.info("redisop 获取 key：{} 对应 value ：{}", key, o);
        return (String) o;
    }

    @Override
    public void set(String key, String obj, Long expireTime) {
        LOG.info("redisop 存放 key:{} , value:{}, expireTime:{}", key, obj, expireTime);
        if (expireTime != null) {
            valueOperations.set(key, obj, expireTime, TimeUnit.SECONDS);
        } else {
            valueOperations.set(key, obj);
        }
    }

    @Override
    public void putInHash(String key, String hKey, String hObj) {
        LOG.info("redisp 存入redis Hash数据, key:{}, hkey;{}, hobj:{}", key, hKey, hObj);
        hashOperations.put(key, hKey, hObj);
    }

    @Override
    public boolean hasKeyInHash(String key, String hKey) {
        Boolean aBoolean = hashOperations.hasKey(key, hKey);
        LOG.info("hKey:{} in key:{}, result:{}", hKey, key, aBoolean);
        return aBoolean;
    }

    @Override
    public String getValueFromHash(String key, String hKey) {
        return null;
    }

    @Override
    public Map<String, String> getEntriesFromHash(String key) {
        return null;
    }

    @Override
    public Long deleteKeysFromHash(String key, String... hashKeys) {
        return null;
    }
}
