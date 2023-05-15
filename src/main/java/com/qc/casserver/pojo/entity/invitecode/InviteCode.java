package com.qc.casserver.pojo.entity.invitecode;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.qc.casserver.utils.RandomName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InviteCode implements Serializable {
    private Long id;

    private String inviteCode;

    /**
     * 是否持久化
     */
    private Integer persistence;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)//只在插入时填充
    private Integer isDeleted;

    private Integer usageCount;

    private Long createUser;

    @TableField(fill = FieldFill.INSERT)//只在插入时填充
    private LocalDateTime createTime;

    public static InviteCode randomOneInviteCode(Long userId,Integer persistence){
        InviteCode inviteCode1 = new InviteCode();
        String uuid = RandomName.getUUID();
        String year = LocalDateTime.now().getYear()%1000+"";
        String hour = LocalDateTime.now().getHour() + "";
        inviteCode1.setInviteCode(year+hour+uuid.substring(1,3));
        inviteCode1.setCreateUser(userId);
        inviteCode1.setCreateTime(LocalDateTime.now());
        inviteCode1.setUsageCount(0);
        inviteCode1.setPersistence(persistence);
        inviteCode1.setIsDeleted(0);
        return inviteCode1;
    }
}
