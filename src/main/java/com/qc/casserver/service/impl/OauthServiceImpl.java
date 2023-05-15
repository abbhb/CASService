package com.qc.casserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.R;
import com.qc.casserver.mapper.OauthMapper;
import com.qc.casserver.pojo.InviteCodeResult;
import com.qc.casserver.pojo.entity.Oauth;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.invitecode.InviteCode;
import com.qc.casserver.service.OauthService;
import com.qc.casserver.utils.RandomName;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;


/**
 对表的操作
 */
@Service
@Slf4j
public class OauthServiceImpl extends ServiceImpl<OauthMapper, Oauth> implements OauthService {

    @Transactional
    @Override
    public boolean removeById(Serializable id) {
        /**
         * LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
         *         lambdaUpdateWrapper.set(User::getDeleteTime,new Date().getTime());
         *         lambdaUpdateWrapper.set(User::getIsDeleted,1);
         *         lambdaUpdateWrapper.eq(User::getId,id);
         *         return this.update(lambdaUpdateWrapper);
         */
        LambdaUpdateWrapper<Oauth> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(Oauth::getIsDeleted,1);
        lambdaUpdateWrapper.set(Oauth::getDeleteTime,new Date().getTime());
        lambdaUpdateWrapper.eq(Oauth::getId,id);
        return this.update(lambdaUpdateWrapper);
    }

    @Transactional
    @Override
    public boolean removeByMap(Map<String, Object> columnMap) {
        throw new CustomException("暂时禁用此方法异常");
    }

    @Transactional
    @Override
    public boolean remove(Wrapper<Oauth> queryWrapper) {
        Oauth one = this.getOne(queryWrapper);
        if (one==null){
            throw new CustomException("未找到此数据");
        }
        return this.removeById(one.getId());
    }

    @Transactional
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            this.removeById(id);
        }
        return true;
    }

    @Transactional
    @Override
    public Oauth addOauth(Oauth oauth) {
        //参数校验
        if (oauth==null){
            throw new CustomException("添加失败");
        }
        //oauth内参数校验
        if (oauth.getGrantType()==null||StringUtils.isEmpty(oauth.getRedirectUri())|| StringUtils.isEmpty(oauth.getClientName())){
            throw new CustomException("添加失败");
        }
        //生成client_id和client_secret
        oauth.setClientId(RandomName.getUUID());
        oauth.setClientSecret(RandomName.getUUID());
        //保存
        boolean save = this.save(oauth);
        if (save){
            return oauth;
        }
        throw new CustomException("添加失败");
    }

    @Override
    public R<PageData<Oauth>> listOauth(Integer pageNum, Integer pageSize) {
        Page<Oauth> pageInfo = new Page(pageNum,pageSize);
        Page page = page(pageInfo);
        if (page==null){
            return R.error("啥也没有");
        }
        PageData<Oauth> pageData = new PageData<>();
        pageData.setRecords(pageInfo.getRecords());
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return R.success(pageData);
    }

    @Transactional
    @Override
    public R<String> delete(Long id) {
        boolean b = removeById(id);
        if (b) {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

    @Transactional
    @Override
    public R<String> editAuth(Oauth oauth) {
        if (oauth==null){
            throw new CustomException("修改失败");
        }
        if (oauth.getId()==null){
            throw new CustomException("修改失败");
        }
        if (StringUtils.isEmpty(oauth.getRedirectUri())){
            throw new CustomException("修改失败");
        }
        if (StringUtils.isEmpty(oauth.getClientName())){
            throw new CustomException("修改失败");
        }
        if (StringUtils.isEmpty(oauth.getClientSecret())){
            throw new CustomException("修改失败");
        }
        if (oauth.getGrantType()==null){
            throw new CustomException("修改失败");
        }
        oauth.setClientId(null);
        boolean b = updateById(oauth);
        if (b){
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }


}
