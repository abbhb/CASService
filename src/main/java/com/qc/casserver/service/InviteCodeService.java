package com.qc.casserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.InviteCodeResult;
import com.qc.casserver.pojo.entity.invitecode.InviteCode;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.invitecode.ListInviteCode;

import java.util.List;

public interface InviteCodeService extends IService<InviteCode> {
    /**
     * 新增个邀请码
     * @param userId 用户ID
     * @param persistence 是否持久化
     * @return
     */
    public InviteCode addOneInviteCode(Long userId,Integer persistence);

    public List<InviteCode> addListInviteCode(Long userId,Integer persistence, Integer numberOfLines);

    public R<PageData<InviteCodeResult>> listMyInviteCode(Integer pageNum, Integer pageSize, Long userId);

    public R<PageData<InviteCodeResult>> listAllInviteCode(Integer pageNum, Integer pageSize, Long userId);

    public boolean deleteListInviteCode(String id,Long userId);
    public boolean deleteOneInviteCode(Long id,Long userId);


    R<InviteCodeResult> addInviteCode(Long userId, InviteCode inviteCode);

    R<List<InviteCodeResult>> addInviteCodeList(Long userId, ListInviteCode listInviteCode);

    boolean useInviteCode(String inviteCode);
}
