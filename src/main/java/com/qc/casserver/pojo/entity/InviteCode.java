package com.qc.casserver.pojo.entity;

import com.qc.casserver.utils.RandomName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;

@Data
public class InviteCode implements Serializable {
    private Long id;

    private String inviteCode;

    private Integer persistence;

    private Integer isDeleted;

    private Integer usageCount;

    private Long createUser;

    private LocalDateTime createTime;

    public static InviteCode randomOneInviteCode(Long userId,Integer persistence){
        InviteCode inviteCode1 = new InviteCode();
        inviteCode1.setInviteCode(RandomName.getUUID());
        inviteCode1.setCreateUser(userId);
        inviteCode1.setCreateTime(LocalDateTime.now());
        inviteCode1.setUsageCount(0);
        inviteCode1.setPersistence(persistence);
        inviteCode1.setIsDeleted(0);
        return inviteCode1;
    }
}
