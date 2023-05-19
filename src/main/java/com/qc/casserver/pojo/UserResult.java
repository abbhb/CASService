package com.qc.casserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResult implements Serializable {

    private String id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String studentId;

    private Integer status;

    private String avatar;


    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    //分组
    private Integer permission;
    //权限名
    private String permissionName;

    //后期可能拓展
    private String email;

    private String tgc;

    //原st，用于传统cas认证
    private String ticket;

    //用于oauth2.0
    private String code;

    //状态码,原样返回，用于防止csrf攻击
    private String state;

    /**
     * 作为cas和oauth的公共参数
     */
    private String redirectUri;

}
