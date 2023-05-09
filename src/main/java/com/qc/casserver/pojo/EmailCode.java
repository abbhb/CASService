package com.qc.casserver.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmailCode implements Serializable {
    private String email;

    /**
     * 验证码
     */
    private String randomCode;//key

    private String verificationCode;
}
