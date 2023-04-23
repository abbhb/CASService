package com.qc.casserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.InviteCodeResult;
import com.qc.casserver.pojo.entity.InviteCode;
import com.qc.casserver.pojo.entity.PageData;

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

    public PageData<InviteCode> listAllInviteCode();

    public boolean deleteListInviteCode(String id,Long userId);
    public boolean deleteOneInviteCode(Long id,Long userId);




}
