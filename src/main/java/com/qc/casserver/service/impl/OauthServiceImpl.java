package com.qc.casserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.R;
import com.qc.casserver.mapper.OauthMapper;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Authorize;
import com.qc.casserver.pojo.entity.Oauth;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.OauthService;
import com.qc.casserver.utils.OAuthUtil;
import com.qc.casserver.utils.ParamsCalibration;
import com.qc.casserver.utils.RandomName;
import com.qc.casserver.utils.TicketUtil;
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
    private final IRedisService iRedisService;

    public OauthServiceImpl(IRedisService iRedisService) {
        this.iRedisService = iRedisService;
    }


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
        if (oauth.getGrantType()==null||StringUtils.isEmpty(oauth.getClientName())){
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


    @Override
    public R<UserResult> loginAggregationReturns(UserResult userResult, Authorize authorize) {
        //tgc登录和账号密码登录公共部分
        //重定向交给前端吧
        if (!ParamsCalibration.haveMust(userResult)) {
            //必要信息不全，必须绑定
            userResult.setTicket(null);
            return R.successOnlyObjectWithStatus(userResult, 308);
        }
        if (StringUtils.isNotEmpty(authorize.getRedirectUri())&&StringUtils.isEmpty(authorize.getResponseType())&&StringUtils.isEmpty(authorize.getCode())) {
            //传统CAS登录
            String ticket = TicketUtil.addNewTicket(userResult.getUsername(), Long.valueOf(userResult.getId()), userResult.getPermission());
            //15过期的st,防止网络缓慢
            iRedisService.setTicket(ticket,userResult.getId());
            if (authorize.getRedirectUri().contains("#")) {
                userResult.setRedirectUri(authorize.getRedirectUri().substring(0, authorize.getRedirectUri().lastIndexOf("#")));
            }else {
                userResult.setRedirectUri(authorize.getRedirectUri());
            }
            //设置票据
            userResult.setTicket(ticket);
            log.info("用户登录成功，userResult：{}", userResult);
            return R.successOnlyObjectWithStatus(userResult, 302);
        }else if (!StringUtils.isEmpty(authorize.getResponseType())&&!StringUtils.isEmpty(authorize.getClientId())){
            //走OAuth2.0
            //判断是否有此客户端
            if (StringUtils.isEmpty(authorize.getClientId())){
                return R.error("未找到此客户端(客户端Id=null)");
            }
            LambdaQueryWrapper<Oauth> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Oauth::getClientId,authorize.getClientId());
            Oauth oauth = this.getOne(lambdaQueryWrapper);
            if (oauth==null){
                return R.error("未找到此客户端");
            }
            //判断客户端是否已经定义了回调地址（定义了就不让随意回调）
            if (StringUtils.isNotEmpty(oauth.getRedirectUri())){
                userResult.setRedirectUri(oauth.getRedirectUri());
            }else {
                userResult.setRedirectUri(authorize.getRedirectUri());
            }
            if (StringUtils.isNotEmpty(authorize.getState())){
                userResult.setState(authorize.getState());
            }
            //判断是否有权限
            if (oauth.getGrantType().equals(1)) {
                //暂时只支持授权码模式
                if (!authorize.getResponseType().equals("code")) {
                    return R.error("此客户端不支持此模式");
                }

            }else {
                return R.error("不支持此客户端的授权模式");
            }
            //生成code
            String code = OAuthUtil.addNewCode(iRedisService, Long.valueOf(userResult.getId()));
            //返回code
            userResult.setCode(code);
            //oauth模式没有ticket
            userResult.setTicket(null);

            /**
             * 定义303作为OAuth2.0的返回
             */
            return R.successOnlyObjectWithStatus(userResult, 303);
        }
        else {
            //正常登录平台
            userResult.setTicket(null);
            userResult.setRedirectUri(null);
            userResult.setCode(null);
            return R.success(userResult);
        }

    }

}
