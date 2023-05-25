package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.*;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.OauthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 保留传统CAS的认证接口（拿ST去换用户信息）
 * 暂时只做接入了oauth2.0的认证
 * 不返回st，直接返回一个code
 */
@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/oauth")
public class AuthController {
    private final AuthService authService;

    private final OauthService oauthService;

    @Autowired
    public AuthController(AuthService authService, OauthService oauthService) {
        this.authService = authService;
        this.oauthService = oauthService;
    }

    /**
     * /oauth/accessToken,/oauth/token	获取accessToken	POST
     * grant_type（必填）– 必须为“authorization_code”
     * 代码（必需）– 从授权服务器返回的代码(code)
     * client_id（必需）– 客户端 ID
     * client_secret（必需）– 客户端密码
     * 状态（可选|必需） – CSRF 令牌。如果启用了“强制状态”，则为必需。
     * @param authorize
     * @return
     */
    @PostMapping("/access_token")
    public R<Token> authorizeCodeTOAccessToken(@RequestBody Authorize authorize){
        if (authorize==null){
            return R.error("访问被拒绝:null");
        }
        if (StringUtils.isEmpty(authorize.getCode())){
            return R.error("访问被拒绝:Code");
        }
        if (StringUtils.isEmpty(authorize.getClientId())){
            return R.error("访问被拒绝:cilentId");
        }
        if (!authorize.getGrantType().equals("authorization_code")){
            return R.error("访问被拒绝:grantType");
        }
        if (StringUtils.isEmpty(authorize.getClientSecret())){
            return R.error("访问被拒绝:clientSecret");
        }
        return authService.addToken(authorize);
    }

    /**
     * /oauth/me	通过access_token参数获取用户信息	GET
     * access_token（必需）– 这必须是有效的访问令牌
     * @param accessToken
     * @return 响应将采用 JSON 格式，包含用户名、电子邮件和有关授权访问令牌的用户的其他用户信息。
     */
    @GetMapping("/me")
    public R<UserResult> getUserByAccessToken(@RequestParam("access_token") String accessToken){
        if (StringUtils.isEmpty(accessToken)){
            return R.error("访问被拒绝");
        }
        return authService.getUserByAccessToken(accessToken);
    }

    /**
     * /oauth/authorize	获取authCode或者token	GET
     * /oauth/authorize	response_type=code&client_id=ID&redirect_uri=CALLBACK	会重定向到redirect_uri这个地址并携带code参数
     * @return
     */
    @GetMapping("/authorize")
    public R<String> authorize(@RequestParam("response_type") String responseType, @RequestParam("client_id") String clientId, @RequestParam(value = "redirect_uri",required = false)String redirectUri,@RequestParam(value = "state",required = false)String state,HttpServletRequest request, HttpServletResponse response){
        if (StringUtils.isEmpty(responseType)){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(clientId)){
            return R.error("访问被拒绝");
        }

        String ServerName = request.getServerName();//返回服务器的主机名
        String red = "http://"+ServerName+":55554/?response_type="+responseType+"&client_id="+clientId;
        if (StringUtils.isNotEmpty(state)){
            red += "&state="+state;
        }
        if (StringUtils.isNotEmpty(redirectUri)){
            red += "&redirect_uri="+redirectUri;
        }
        try {
            response.sendRedirect(red);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @PostMapping("/refresh_token")
    public R<Token> refreshToken(@RequestBody Token token){
        if (token==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(token.getRefreshToken())){
            return R.error("访问被拒绝");
        }
        return authService.refreshToken(token.getRefreshToken());
    }

    @PostMapping("/logout_token")
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
        if (oauth.getGrantType()==null){
            return R.error("请提供认证类型");
        }
        Oauth oauth1 = oauthService.addOauth(oauth);
        return R.success(oauth1);
    }

    @GetMapping("/list")
    @PermissionCheck("10")
    @NeedLogin
    public R<PageData<Oauth>> listOauth(@RequestParam("page_num") Integer pageNum, @RequestParam("page_size") Integer pageSize){
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
        if (oauth.getGrantType()==null){
            return R.error("请提供认证类型");
        }
        if (StringUtils.isEmpty(oauth.getClientSecret())){
            return R.error("请提供完整ClientSecret");
        }
        return oauthService.editAuth(oauth);
    }


}
