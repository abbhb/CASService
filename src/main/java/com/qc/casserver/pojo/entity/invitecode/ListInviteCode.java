package com.qc.casserver.pojo.entity.invitecode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ListInviteCode extends InviteCode{
    @JsonProperty("number_of_lines")
    private Integer numberOfLines;
}
