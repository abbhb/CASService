package com.qc.casserver.service.impl;



import com.qc.casserver.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.qc.casserver.common.MyString.ST_PRE;

@Service
public class IRedisServiceImpl implements IRedisService {

    private RedisTemplate redisTemplate;

    @Autowired
    public IRedisServiceImpl(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
//    @Override
//    public String getTokenId(String token) {
//        return (String)redisTemplate.opsForValue().get(token);
//    }

    @Override
    public void setTokenWithTime(String tgc, String tgt, Long time) {
        redisTemplate.opsForValue().set(tgc, tgt, time, TimeUnit.SECONDS);
    }

    @Override
    public void setWithTime(String key, String value, Long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }
    @Override
    public void setWithTime(String key, Object value, Long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    @Override
    public void del(String token) {
        redisTemplate.delete(token);

    }
    @Override
    public void hashPut(String key,String hashKey,Object object) {
        redisTemplate.opsForHash().put(key,hashKey,object);
    }

    @Override
    public Object getHash(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key,hashKey);
    }

    @Override
    public Long getTokenTTL(String uuid) {
        Long expire = redisTemplate.getExpire(uuid);
        return expire;
    }

    @Override
    public void setTTL(String key,Long time) {
        redisTemplate.expire(key,time , TimeUnit.SECONDS);
    }

    @Override
    public String getValue(String key) {
        return (String)redisTemplate.opsForValue().get(key);
    }
    @Override
    public Object getValueObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }


    @Override
    public String getSTValue(String key) {
        return (String)redisTemplate.opsForValue().get(ST_PRE+key);
    }

    @Override
    public void setST(String st, String value) {
        redisTemplate.opsForValue().set(ST_PRE+st, value, 15L, TimeUnit.SECONDS);
    }

}
