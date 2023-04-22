package com.qc.casserver.pojo.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Token implements Serializable {
    private String accessToken;

    private String refreshToken;

    private LocalDateTime accessTokenExpiredTime;

    private LocalDateTime refreshTokenExpiredTime;

    private String sign;

}

