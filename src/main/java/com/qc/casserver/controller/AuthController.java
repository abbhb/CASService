package com.qc.casserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Oauth;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.pojo.entity.Token;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.OauthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 保留传统CAS的认证接口（拿ST去换用户信息）
 * 暂时只做接入了oauth2.0的认证
 * 不返回st，直接返回一个code
 */
@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/oauth2.0")
public class AuthController {
    private final AuthService authService;

    private final OauthService oauthService;

    @Autowired
    public AuthController(AuthService authService, OauthService oauthService) {
        this.authService = authService;
        this.oauthService = oauthService;
    }

    /**
     * /oauth2.0/accessToken,/oauth2.0/token	获取accessToken	POST
     * @param ticket
     * @return
     */
    @PostMapping("/accessToken")
    public R<Token> sTToAccessToken(@RequestBody Ticket ticket){
        if (ticket==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(ticket.getSt())){
            return R.error("访问被拒绝");
        }
        return authService.addToken(ticket.getSt());
    }

    /**
     * /oauth2.0/profile	通过access_token参数获取用户信息	GET
     * @param token
     * @return
     */
    @GetMapping("/profile")
    public R<UserResult> getUserByAccessToken(@RequestBody Token token){
        if (token==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(token.getAccessToken())){
            return R.error("访问被拒绝");
        }
        return authService.getUserByAccessToken(token.getAccessToken());
    }

    /**
     * /oauth2.0/authorize	获取authCode或者token	GET
     * /oauth2.0/authorize	response_type=code&client_id=ID&redirect_uri=CALLBACK	会重定向到redirect_uri这个地址并携带authCode参数
     * @param token
     * @return
     */
    @GetMapping("/authorize")
    public R<UserResult> authorize(@RequestBody Token token){
        if (token==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(token.getAccessToken())){
            return R.error("访问被拒绝");
        }
        return authService.getUserByAccessToken(token.getAccessToken());
    }

    @PostMapping("/refreshToken")
    public R<Token> refreshToken(@RequestBody Token token){
        if (token==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(token.getRefreshToken())){
            return R.error("访问被拒绝");
        }
        return authService.refreshToken(token.getRefreshToken());
    }

    @PostMapping("/logoutToken")
    public R<String> logoutToken(@RequestBody Token token){
        if (token==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(token.getAccessToken())){
            return R.error("请提供完整AccessToken");
        }
        if (StringUtils.isEmpty(token.getRefreshToken())){
            return R.error("请提供完整RefreshToken");
        }
        return authService.logoutToken(token);
    }

    @NeedLogin
    @PermissionCheck("10")
    @PostMapping("/addAuth")
    public R<Oauth> addAuth(@RequestBody Oauth oauth){
        if (oauth==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(oauth.getClientName())){
            return R.error("请提供完整ClientName");
        }
        if (StringUtils.isEmpty(oauth.getRedirectUri())){
            return R.error("请提供完整回调地址");
        }
        if (oauth.getGrantType()==null){
            return R.error("请提供认证类型");
        }
        Oauth oauth1 = oauthService.addOauth(oauth);
        return R.success(oauth1);
    }

    @GetMapping("/list")
    @PermissionCheck("10")
    @NeedLogin
    public R<PageData<Oauth>> listOauth(Integer pageNum, Integer pageSize){
        if (pageNum==null||pageSize==null){
            return R.error("查询失败");
        }
        return oauthService.listOauth(pageNum,pageSize);
    }

    @DeleteMapping("/delete")
    @PermissionCheck("10")
    @NeedLogin
    public R<String> delete(Long id){
        if (id==null){
            return R.error("删除失败");
        }
        return oauthService.delete(id);
    }

    @PutMapping("/editAuth")
    @PermissionCheck("10")
    @NeedLogin
    public R<String> editAuth(@RequestBody Oauth oauth){
        if (oauth==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(oauth.getClientName())){
            return R.error("请提供完整ClientName");
        }
        if (StringUtils.isEmpty(oauth.getRedirectUri())){
            return R.error("请提供完整回调地址");
        }
        if (oauth.getGrantType()==null){
            return R.error("请提供认证类型");
        }
        if (StringUtils.isEmpty(oauth.getClientSecret())){
            return R.error("请提供完整ClientSecret");
        }
        return oauthService.editAuth(oauth);
    }


}
