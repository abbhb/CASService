package com.qc.casserver.pojo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qc.casserver.utils.RandomName;
import lombok.Data;

import java.io.Serializable;

/**
 * 传参使用，非实体类
 */
@Data
public class Authorize implements Serializable {
    //authorizeCode
    private String code;

    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("response_type")
    private String responseType;
    @JsonProperty("redirect_uri")
    private String redirectUri;

    /**
     * 状态码,原样返回，用于防止csrf攻击
     */
    private String state;

    /**
     * 获取身份验证代码和获取访问令牌的客户端的标识符
     */
    @JsonProperty("client_id")
    private String clientId;


    /**
     * 用于获取令牌时校验
     */
    @JsonProperty("client_secret")
    private String clientSecret;

    private String scope;

    public String addNewCode() {
        //生成base64编码的40位随机字符串不包含符号
        String code = RandomName.getUUID();
        return code;
    }

    /**
     * 用于传统CAS认证
     * 接受参数
     */
    private String service;

}
