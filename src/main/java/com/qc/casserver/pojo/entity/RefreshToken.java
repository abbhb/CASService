package com.qc.casserver.pojo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.PipedReader;
import java.io.Serializable;

/**
 * 用来记录当前是否有token没过期
 */
@Data
public class RefreshToken implements Serializable {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("access_token")
    private String accessToken;
}
