package com.qc.casserver.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmailCode implements Serializable {
    private String email;

    /**
     * 验证码
     */
    @JsonProperty("random_code")
    private String randomCode;//key

    @JsonProperty("verification_code")
    private String verificationCode;

    /**
     * 邮箱验证码
     */
    @JsonProperty("email_code")
    private String emailCode;
}
