package com.qc.casserver.pojo.vo;

import com.qc.casserver.pojo.entity.User;
import lombok.Data;

@Data
public class RegisterUser extends User {

    /**
     * 邮箱验证码
     */
    private String mailCode;


    /**
     * 邀请码
     */
    private String inviteCode;

    
    /**
     * 验证码
     */
    private String verificationCode;
}
