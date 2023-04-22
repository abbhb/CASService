package com.qc.casserver.service.impl;

import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.RefreshToken;
import com.qc.casserver.pojo.entity.Token;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.UserService;
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

    @Autowired
    public AuthServiceImpl(IRedisService iRedisService, UserService userService) {
        this.iRedisService = iRedisService;
        this.userService = userService;
    }

    @Override
    public R<Token> addToken(String st) {
        UUID uuid = UUID.randomUUID();
        UUID uuid1 = UUID.randomUUID();
        String accessToken = uuid.toString();
        String refreshToken = uuid1.toString();
        if (StringUtils.isEmpty(st)){
            throw new CustomException("认证失败");
        }
        String userId= iRedisService.getSTValue(st);
        log.info("st={},stvalue={}",st,userId);
        if (StringUtils.isEmpty(userId)){
            throw new CustomException("认证失败");
        }
        //accessToken 3小时
        iRedisService.setWithTime(accessToken,userId,3*3600L);
        //refreshToken 12小时 并且存了accessToken
        RefreshToken refreshToken1 = new RefreshToken();
        refreshToken1.setAccessToken(accessToken);
        refreshToken1.setUserId(Long.valueOf(userId));
        iRedisService.setWithTime(refreshToken,refreshToken1,12*3600L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime3 = now.plusHours(3);
        LocalDateTime localDateTime12 = now.plusHours(12);
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setSign("DangDangDang");
        token.setAccessTokenExpiredTime(localDateTime3);
        token.setRefreshTokenExpiredTime(localDateTime12);
        return R.success(token);
    }

    @Override
    public R<UserResult> getUserByAccessToken(String accessToken) {
        if (StringUtils.isEmpty(accessToken)){
            throw new CustomException("认证失败");
        }
        String userId = iRedisService.getValue(accessToken);
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
        userResult.setPermissionName(byId.getName());
        userResult.setPhone(byId.getPhone());
        userResult.setSex(byId.getSex());
        return R.success(userResult);
    }

    @Override
    public R<Token> refreshToken(String refreshToken) {
        if (StringUtils.isEmpty(refreshToken)){
            throw new CustomException("刷新失败");
        }
        //先判断是否存在没有过期的accesstoken
        RefreshToken refreshToken1 = (RefreshToken) iRedisService.getValueObject(refreshToken);
        if (refreshToken1==null){
            //这个refreshToken过期了
            return R.error("你的refreshToken过期了");
        }
        String accessToken = refreshToken1.getAccessToken();
        Long tokenTTL = iRedisService.getTokenTTL(accessToken);
        if (tokenTTL>10){
            //还没过期
            //再给3小时
            iRedisService.setTTL(accessToken,3*3600L);

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
        String uuidString = uuid.toString();
        iRedisService.setWithTime(uuidString,String.valueOf(refreshToken1.getUserId()),3*3600L);
        Token token = new Token();
        token.setAccessToken(uuidString);
        token.setRefreshToken(refreshToken);
        token.setSign("DangDangDang");
        token.setAccessTokenExpiredTime(LocalDateTime.now().plusHours(3));
        token.setRefreshTokenExpiredTime(LocalDateTime.now().plusSeconds(tokenTTL));
        return R.successOnlyObjectWithStatus(token,2);
    }
}
