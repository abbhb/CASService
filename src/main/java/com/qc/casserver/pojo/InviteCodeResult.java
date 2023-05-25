package com.qc.casserver.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InviteCodeResult implements Serializable {
    private String id;

    @JsonProperty("invite_code")
    private String inviteCode;

    private Integer persistence;

    @JsonProperty("usage_count")
    private Integer usageCount;

    @JsonProperty("create_user")
    private String createUser;

    @JsonProperty("create_time")
    private LocalDateTime createTime;
}
