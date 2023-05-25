package com.qc.casserver.pojo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qc.casserver.pojo.entity.User;
import lombok.Data;

@Data
public class RegisterUser extends User {

    /**
     * 邮箱验证码
     */
    @JsonProperty("mail_code")
    private String mailCode;

    /**
     * 邀请码
     */
    @JsonProperty("invite_code")
    private String inviteCode;

    @JsonProperty("re_password")
    private String rePassword;
}
