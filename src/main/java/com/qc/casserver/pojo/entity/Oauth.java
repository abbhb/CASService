package com.qc.casserver.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Oauth implements Serializable {
    /**
     * CREATE TABLE `oauth` (
     *   `id` bigint NOT NULL,
     *   `client_id` varchar(255) NOT NULL COMMENT '客户端id',
     *   `client_secret` varchar(255) NOT NULL COMMENT '客户端秘钥',
     *   `redirect_uri` varchar(255) NOT NULL COMMENT '回调地址，成功回调会自动加上返回参数code',
     *   `client_name` varchar(255) NOT NULL COMMENT '客户端name',
     *   `grant_type` int NOT NULL DEFAULT '1' COMMENT '授权类型：grant_type ，1为authorization_code（授权码模式）',
     *   `create_time` datetime DEFAULT NULL,
     *   `update_time` datetime DEFAULT NULL,
     *   `is_deleted` int NOT NULL DEFAULT '0',
     *   PRIMARY KEY (`id`),
     *   UNIQUE KEY `name` (`client_name`) USING BTREE,
     *   UNIQUE KEY `client_id` (`client_id`) USING BTREE
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
     */

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
    private String clientId;

    /**
     * 客户端秘钥
     */
    private String clientSecret;
    /**
     * 回调地址，成功回调会自动加上返回参数code
     */
    private String redirectUri;

    /**
     * 客户端name
     */
    private String clientName;

    /**
     * 授权类型：grant_type ，1为authorization_code（授权码模式）
     */
    private Integer grantType;


    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;

    /**
     * 删除时间,这个字段不会自动填充，需要自己写sql
     * 解决方法，重写removeById方法，自己写sql
     */
    private Long deleteTime = 1L;

}
