package com.qc.casserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.R;
import com.qc.casserver.mapper.InviteCodeMapper;
import com.qc.casserver.pojo.InviteCodeResult;
import com.qc.casserver.pojo.entity.InviteCode;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.InviteCodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InviteCodeServiceImpl extends ServiceImpl<InviteCodeMapper, InviteCode> implements InviteCodeService {
    private final IRedisService iRedisService;

    @Autowired
    public InviteCodeServiceImpl(IRedisService iRedisService) {
        this.iRedisService = iRedisService;
    }

    @Transactional
    @Override
    public InviteCode addOneInviteCode(Long userId,Integer persistence) {
        InviteCode inviteCode = InviteCode.randomOneInviteCode(userId,persistence);
        boolean save = super.save(inviteCode);
        if (!save){
            throw new CustomException("新增失败");
        }
        return inviteCode;
    }

    @Transactional
    @Override
    public List<InviteCode> addListInviteCode(Long userId, Integer persistence, Integer numberOfLines) {
        List<InviteCode> inviteCodeList = new ArrayList<>();
        for (int i=0;i<numberOfLines;i++){
            InviteCode inviteCode = InviteCode.randomOneInviteCode(userId, persistence);
            boolean save = super.save(inviteCode);
            if (!save){
                throw new CustomException("业务异常");
            }
            inviteCodeList.add(inviteCode);
        }
        return inviteCodeList;
    }

    @Override
    public R<PageData<InviteCodeResult>> listMyInviteCode(Integer pageNum, Integer pageSize, Long userId) {
        if (pageSize==null||pageSize==null||userId==null){
            throw new CustomException("参数异常");
        }
        Page<InviteCode> pageInfo = new Page(pageNum,pageSize);
        LambdaQueryWrapper<InviteCode> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(InviteCode::getCreateTime);
        lambdaQueryWrapper.eq(InviteCode::getCreateUser,userId);
        //暂时不支持通过日期模糊查询
        Page page = super.page(pageInfo, lambdaQueryWrapper);
        if (page==null){
            return R.error("啥也没有");
        }
        PageData<InviteCodeResult> pageData = new PageData<>();
        List<InviteCodeResult> results = new ArrayList<>();
        for (InviteCode inviteCodeitem : pageInfo.getRecords()) {
            InviteCodeResult inviteCodeResult1 = new InviteCodeResult();
            inviteCodeResult1.setInviteCode(inviteCodeitem.getInviteCode());
            inviteCodeResult1.setId(String.valueOf(inviteCodeitem.getId()));
            inviteCodeResult1.setPersistence(inviteCodeitem.getPersistence());
            inviteCodeResult1.setCreateTime(inviteCodeitem.getCreateTime());
            inviteCodeResult1.setUsageCount(inviteCodeitem.getUsageCount());
            results.add(inviteCodeResult1);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());

        return R.success(pageData);

    }

    @Override
    public PageData<InviteCode> listAllInviteCode() {
        return null;
    }

    @Override
    public boolean deleteListInviteCode(String id, Long userId) {
        return false;
    }

    @Override
    public boolean deleteOneInviteCode(Long id, Long userId) {
        return false;
    }


}
