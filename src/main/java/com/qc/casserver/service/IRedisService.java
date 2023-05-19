package com.qc.casserver.service;

import java.util.Set;

public interface IRedisService {
//    String getTokenId(String token);

    void addTGCWithTGT(String tgc,String tgt,Long time);

    Long getTGCTTL(String tgc);

    void setWithTime(String key,String value,Long time);

    void setWithTime(String key,Object value,Long time);

    void addAccessToken(String key,Object value,Long time);


    void addAuthorizeCode(String authorizeCode,String userId);

    String getAuthorizeCode(String authorizeCode);

    void deleteAuthorizeCode(String authorizeCode);

    Set<String> getLogout(String userId);

    void delLogoutOne(String userId,String key);

    void addRefreshToken(String key,Object value,Long time);

    void delRefreshToken(String key);
    void delAccessToken(String key);


    Object getRefreshToken(String key);
    Object getAccessToken(String key);

    Long getAccessTokenTTL(String uuid);
    Long getRefreshTokenTTL(String uuid);

    void setAccessTokenTTL(String key,Long time);

    void setRefreshTokenTTL(String key,Long time);

    void del(String token);

    Long getTokenTTL(String uuid);

    void setTTL(String key,Long time);
    String getValue(String key);
    Object getValueObject(String key);

    String getSTValue(String key);
    void setTicket(String ticket, String value);
    void hashPut(String key,String hashKey,Object object);

    Object getHash(String key, String hashKey);

    void setTGCTTL(String tgc, long l);

    String getTGT(String tgc);

    void delLogout(Long userId);

    Long getLogoutSize(String userId);
}
