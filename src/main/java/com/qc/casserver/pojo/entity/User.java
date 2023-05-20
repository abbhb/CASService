package com.qc.casserver.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 逻辑删除和唯一索引冲突，所以重写了remove方法
 */
@Data
@AllArgsConstructor//不加这个是没有有参构造的
@NoArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    //value属性用于指定主键的字段
    //type属性用于设置主键生成策略，默认雪花算法


    private String password;

    /**
     * 盐
     */
    private String salt;

    @TableField(fill = FieldFill.INSERT)//只在插入时填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//这些注解都是调用basemapper才有用,自己写的sql不会生效，插入和更新时都填充
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic//如果加了这个字段就说明这个表里默认都是假删除，mp自带的删除方法都是改状态为1，默认0是不删除。自定义的mybatis得自己写
    private Integer isDeleted;

    @TableId("id")//设置默认主键
    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    //学号
    private Long studentId;

    private Integer status;

    private Integer permission;

    //绑定邮箱
    private String email;

    private String avatar;

    /**
     * 只有当删除时设置为时间戳，其余时候为1L
     */
    private Long deleteTime = 1L;

    /**
     * 开放id(唯一)
     */
    private String openid;

}
