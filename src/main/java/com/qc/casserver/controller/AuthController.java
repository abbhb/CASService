package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.pojo.entity.Token;
import com.qc.casserver.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/api2/oauth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/")
    public R<Token> sTToAccessToken(@RequestBody Ticket ticket){
        if (ticket==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(ticket.getSt())){
            return R.error("访问被拒绝");
        }
        return authService.addToken(ticket.getSt());
    }

    @PostMapping("/accesstoken")
    public R<UserResult> getUserByAccessToken(@RequestBody Token token){
        if (token==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(token.getAccessToken())){
            return R.error("访问被拒绝");
        }
        return authService.getUserByAccessToken(token.getAccessToken());
    }

    @PostMapping("/refreshtoken")
    public R<Token> refreshToken(@RequestBody Token token){
        if (token==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(token.getRefreshToken())){
            return R.error("访问被拒绝");
        }
        return authService.refreshToken(token.getRefreshToken());
    }

}
