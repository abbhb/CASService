package com.qc.casserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.MyString;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.*;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.OauthService;
import com.qc.casserver.service.UserService;
import com.qc.casserver.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final IRedisService iRedisService;

    private final UserService userService;

    private final OauthService oauthService;

    @Autowired
    public AuthServiceImpl(IRedisService iRedisService, UserService userService, OauthService oauthService) {
        this.iRedisService = iRedisService;
        this.userService = userService;
        this.oauthService = oauthService;
    }


    @Override
    public R<Token> addToken(Authorize authorize) {
        if (authorize==null){
            throw new CustomException("认证失败");
        }
        if (StringUtils.isEmpty(authorize.getClientId())){
            throw new CustomException("认证失败");
        }
        if (StringUtils.isEmpty(authorize.getCode())){
            throw new CustomException("认证失败");
        }
        if (StringUtils.isEmpty(authorize.getClientSecret())){
            throw new CustomException("认证失败");
        }
        LambdaQueryWrapper<Oauth> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Oauth::getClientId,authorize.getClientId());
        //校验clientSecret
        Oauth oauth = oauthService.getOne(lambdaQueryWrapper);
        if (oauth==null){
            throw new CustomException("认证失败");
        }
        if (!oauth.getClientSecret().equals(authorize.getClientSecret())){
            throw new CustomException("认证失败");
        }
        UUID uuid = UUID.randomUUID();
        UUID uuid1 = UUID.randomUUID();
        String accessToken = uuid.toString();
        String refreshToken = uuid1.toString();
        //通过ST生成token
        String userId= iRedisService.getAuthorizeCode(authorize.getCode());
        log.info("code={},stvalue={}",authorize.getCode(),userId);
        if (StringUtils.isEmpty(userId)){
            throw new CustomException("认证失败");
        }
        //accessToken 3小时
        iRedisService.addAccessToken(accessToken,userId,3*3600L);
        //refreshToken 12小时 并且存了accessToken
        RefreshToken refreshToken1 = new RefreshToken();
        refreshToken1.setAccessToken(accessToken);
        refreshToken1.setUserId(Long.valueOf(userId));
        iRedisService.addRefreshToken(refreshToken,refreshToken1,12*300L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime3 = now.plusHours(3);
        LocalDateTime localDateTime12 = now.plusHours(12);
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setSign("DangDangDang");
        token.setAccessTokenExpiredTime(localDateTime3);
        token.setRefreshTokenExpiredTime(localDateTime12);
        //删除code
        iRedisService.deleteAuthorizeCode(authorize.getCode());
        return R.success(token);
    }

    @Override
    public R<UserResult> getUserByAccessToken(String accessToken) {
        if (StringUtils.isEmpty(accessToken)){
            throw new CustomException("认证失败");
        }
        String userId = (String) iRedisService.getAccessToken(accessToken);
        if (StringUtils.isEmpty(userId)){
            throw new CustomException("认证失败");
        }
        User byId = userService.getById(Long.valueOf(userId));
        if (byId==null){
            throw new CustomException("业务异常");
        }
        if(byId.getStatus() == 0){
            throw new CustomException("账号已禁用!");
        }
        UserResult userResult = new UserResult();
        userResult.setName(byId.getName());
        userResult.setEmail(byId.getEmail());
        userResult.setStudentId(String.valueOf(byId.getStudentId()));
        userResult.setUsername(byId.getUsername());
        userResult.setCreateTime(byId.getCreateTime());
        userResult.setUpdateTime(byId.getUpdateTime());
        userResult.setAvatar(byId.getAvatar());
        userResult.setStatus(byId.getStatus());
        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(byId.getPermission()));
        if (permission==null){
            throw new CustomException("业务异常");
        }
        userResult.setPermissionName(permission.getName());
        userResult.setPhone(byId.getPhone());
        userResult.setSex(byId.getSex());
        userResult.setOpenid(byId.getOpenid());
        return R.success(userResult);
    }

    @Override
    public R<Token> refreshToken(String refreshToken) {
        if (StringUtils.isEmpty(refreshToken)){
            throw new CustomException("刷新失败");
        }
        //先判断是否存在没有过期的accesstoken
        RefreshToken refreshToken1 = (RefreshToken) iRedisService.getRefreshToken(refreshToken);
        if (refreshToken1==null){
            //这个refreshToken过期了
            return R.error("你的refreshToken过期了");
        }
        String accessToken = refreshToken1.getAccessToken();
        Long tokenTTL = iRedisService.getAccessTokenTTL(accessToken);
        if (tokenTTL>10){
            //还没过期
            //再给3小时
            iRedisService.setAccessTokenTTL(accessToken,3*3600L);

            Token token = new Token();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setSign("DangDangDang");
            token.setAccessTokenExpiredTime(LocalDateTime.now().plusHours(3));
            token.setRefreshTokenExpiredTime(LocalDateTime.now().plusSeconds(tokenTTL));
            return R.successOnlyObjectWithStatus(token,1);
        }
        //已经过期了，换新的accessToken
        UUID uuid = UUID.randomUUID();
        String newAccessToken = uuid.toString();
        iRedisService.addAccessToken(newAccessToken,String.valueOf(refreshToken1.getUserId()),3*3600L);
        Token token = new Token();
        token.setAccessToken(newAccessToken);
        token.setRefreshToken(refreshToken);
        token.setSign("DangDangDang");
        token.setAccessTokenExpiredTime(LocalDateTime.now().plusHours(3));
        token.setRefreshTokenExpiredTime(LocalDateTime.now().plusSeconds(tokenTTL));
        return R.successOnlyObjectWithStatus(token,2);
    }

    @Override
    public R<String> logoutToken(Token token) {
        if (token==null){
            throw new CustomException("token不能为空");
        }
        if (StringUtils.isEmpty(token.getAccessToken())){
            throw new CustomException("AccessToken不能为空");

        }
        if (StringUtils.isEmpty(token.getRefreshToken())){
            throw new CustomException("RefreshToken不能为空");
        }
        iRedisService.delAccessToken(token.getAccessToken());
        iRedisService.delRefreshToken(token.getRefreshToken());
        return R.success("下线成功");
    }

    @Override
    public R<UserResult> getUserInfoByST(String st,String service) {
        if (StringUtils.isEmpty(st)){
            log.info("122");

            return null;
        }
        String userId = (String) iRedisService.getSTValue(st);
        if (StringUtils.isEmpty(userId)){
            log.info("17");
            return null;
        }
        User byId = userService.getById(Long.valueOf(userId));
        if (byId==null){
            log.info("15");

            return null;
        }
        if(byId.getStatus().equals(0)){
            log.info("13");

            return null;
        }

        if (StringUtils.isEmpty(st)){
            log.info("12");
            return null;
        }
        //验证ticket是不是该服务的
        try {
            String tgt = JWTUtil.getTGT(st, service);
        } catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return null;

        }
        UserResult userResult = new UserResult();
        userResult.setId(String.valueOf(byId.getId()));
        userResult.setName(byId.getName());
        userResult.setEmail(byId.getEmail());
        userResult.setStudentId(String.valueOf(byId.getStudentId()));
        userResult.setUsername(byId.getUsername());
        userResult.setCreateTime(byId.getCreateTime());
        userResult.setUpdateTime(byId.getUpdateTime());
        userResult.setAvatar(byId.getAvatar());
        userResult.setStatus(byId.getStatus());
        userResult.setPermission(byId.getPermission());
        userResult.setPhone(byId.getPhone());
        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(byId.getPermission()));
        if (permission==null){
            log.info("112");

            return null;
        }
        userResult.setPermissionName(permission.getName());
        log.info("121231");

        return R.success(userResult);
    }
}
