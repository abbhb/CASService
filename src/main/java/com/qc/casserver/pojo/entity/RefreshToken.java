package com.qc.casserver.pojo.entity;

import lombok.Data;

import java.io.PipedReader;
import java.io.Serializable;

/**
 * 用来记录当前是否有token没过期
 */
@Data
public class RefreshToken implements Serializable {
    private Long userId;

    private String accessToken;
}
