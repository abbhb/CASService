package com.qc.casserver.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InviteCodeResult implements Serializable {
    private String id;

    private String inviteCode;

    private Integer persistence;

    private Integer usageCount;

    private String createUser;

    private LocalDateTime createTime;
}
