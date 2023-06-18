package com.qc.casserver.pojo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleAuthenticator {
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 客户端id
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 客户端秘钥
     */
    @JsonProperty("secret")
    private String secret;

    /**
     * 1:开启
     * 0:关闭
     */
    private Integer state;

    /**
     * 1:开启
     * 0:关闭
     */
    private Integer verify;


}
