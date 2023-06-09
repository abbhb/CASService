package com.qc.casserver.service.impl;



import com.qc.casserver.common.MyString;
import com.qc.casserver.pojo.entity.RefreshToken;
import com.qc.casserver.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.qc.casserver.common.MyString.ST_PRE;

@Service
public class IRedisServiceImpl implements IRedisService {

    private final RedisTemplate redisTemplate;

    @Autowired
    public IRedisServiceImpl(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
//    @Override
//    public String getTokenId(String token) {
//        return (String)redisTemplate.opsForValue().get(token);
//    }

    @Override
    public void addTGCWithTGT(String tgc, String tgt, Long time) {
        redisTemplate.opsForValue().set(MyString.pre_tgc+tgc, tgt, time, TimeUnit.SECONDS);
    }

    @Override
    public Long getTGCTTL(String tgc) {
        Long expire = redisTemplate.getExpire(MyString.pre_tgc+tgc);
        return expire;
    }

    @Override
    public void setWithTime(String key, String value, Long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }
    @Override
    public void setWithTime(String key, Object value, Long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 以下都同步操作logout记录
     */
    /**
     * 往redis里存入accessToken
     * @param key
     * @param value userId
     * @param time Seconds
     */
    @Override
    public void addAccessToken(String key,Object value,Long time) {
        redisTemplate.opsForValue().set(MyString.pre_access_token + key, value, time, TimeUnit.SECONDS);
        String s = MyString.pre_logout(Long.valueOf((String) value));
        redisTemplate.opsForSet().add(s, MyString.pre_access_token + key);
    }

    @Override
    public void addAuthorizeCode(String authorizeCode,String userId) {
        redisTemplate.opsForValue().set(MyString.pre_auth_code + authorizeCode, userId, 30L, TimeUnit.SECONDS);
    }

    @Override
    public String getAuthorizeCode(String authorizeCode) {
        String userId = (String) redisTemplate.opsForValue().get(MyString.pre_auth_code + authorizeCode);
        assert userId != null;
        return userId;
    }

    @Override
    public void deleteAuthorizeCode(String authorizeCode) {
        redisTemplate.delete(MyString.pre_auth_code + authorizeCode);
    }

    @Override
    public Set<String> getLogout(String userId) {
        String s = MyString.pre_logout(Long.valueOf(userId));
        Set<String> members = redisTemplate.opsForSet().members(s);
        return members;
    }

    @Override
    public void delLogoutOne(String userId, String key) {
        String s = MyString.pre_logout(Long.valueOf(userId));
        redisTemplate.opsForSet().remove(s, key);
    }

    /**
     * 往redis里存入refreshToken
     * @param key
     * @param value
     * @param time Seconds
     */
    @Override
    public void addRefreshToken(String key,Object value,Long time) {
        redisTemplate.opsForValue().set(MyString.pre_refresh_token + key, value, time, TimeUnit.SECONDS);
        String s = MyString.pre_logout(((RefreshToken) value).getUserId());
        redisTemplate.opsForSet().add(s, MyString.pre_refresh_token + key);
    }

    @Override
    public void delRefreshToken(String key) {
        Object o = redisTemplate.opsForValue().get(MyString.pre_refresh_token + key);
        if (o!=null){
            String s = MyString.pre_logout(((RefreshToken) o).getUserId());
            redisTemplate.opsForSet().remove(s, key);
        }
        redisTemplate.delete(MyString.pre_refresh_token + key);
    }

    @Override
    public void delAccessToken(String key) {
        Object o = redisTemplate.opsForValue().get(MyString.pre_access_token + key);
        if (o!=null){
            String s = MyString.pre_logout(Long.valueOf((String) o));
            redisTemplate.opsForSet().remove(s, key);
        }
        redisTemplate.delete(MyString.pre_access_token + key);

    }

    @Override
    public void setAccessTokenTTL(String key, Long time) {
        Object o = redisTemplate.opsForValue().get(MyString.pre_access_token + key);
        if (o!=null){
            String s = MyString.pre_logout(Long.valueOf((String) o));
            redisTemplate.opsForSet().add(s, MyString.pre_access_token + key);
        }
        redisTemplate.expire(MyString.pre_access_token + key,time , TimeUnit.SECONDS);
    }

    @Override
    public void setRefreshTokenTTL(String key, Long time) {
        Object o = redisTemplate.opsForValue().get(MyString.pre_refresh_token + key);
        if (o!=null){
            String s = MyString.pre_logout(((RefreshToken) o).getUserId());
            redisTemplate.opsForSet().add(s, MyString.pre_refresh_token + key);
        }
        redisTemplate.expire(MyString.pre_refresh_token + key,time , TimeUnit.SECONDS);
    }

    @Override
    public Object getRefreshToken(String key) {
        return redisTemplate.opsForValue().get(MyString.pre_refresh_token + key);
    }

    @Override
    public Object getAccessToken(String key) {
        return redisTemplate.opsForValue().get(MyString.pre_access_token + key);
    }

    @Override
    public Long getAccessTokenTTL(String uuid) {
        Long expire = redisTemplate.getExpire(MyString.pre_access_token + uuid);
        return expire;
    }

    @Override
    public Long getRefreshTokenTTL(String uuid) {
        Long expire = redisTemplate.getExpire(MyString.pre_refresh_token + uuid);
        return expire;
    }



    @Override
    public void del(String token) {
        redisTemplate.delete(token);

    }
    @Override
    public void delLogout(Long userId){
        redisTemplate.delete(MyString.pre_logout(userId));
    }

    @Override
    public Long getLogoutSize(String userId) {
        //获取集合的大小
        Long size = redisTemplate.opsForSet().size(MyString.pre_logout(Long.valueOf(userId)));
        return size;
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
    public void setTGCTTL(String tgc, long l) {
        redisTemplate.expire(MyString.pre_tgc + tgc,l , TimeUnit.SECONDS);
    }

    @Override
    public String getTGT(String tgc) {
        return (String)redisTemplate.opsForValue().get(MyString.pre_tgc + tgc);
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
    public void setTicket(String ticket, String value) {
        redisTemplate.opsForValue().set(ST_PRE+ticket, value, 60L, TimeUnit.SECONDS);
    }


}
