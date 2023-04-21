package com.qc.casserver.service;

public interface IRedisService {
//    String getTokenId(String token);

    void setTokenWithTime(String tgc,String tgt,Long time);

    void setWithTime(String key,String value,Long time);



    void del(String token);

    Long getTokenTTL(String uuid);

    void setTTL(String key,Long time);
    String getValue(String key);

    String getSTValue(String key);
    void setST(String st,String value);
    void hashPut(String key,String hashKey,Object object);

    Object getHash(String key, String hashKey);
}
