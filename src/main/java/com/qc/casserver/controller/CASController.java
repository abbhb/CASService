package com.qc.casserver.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.pojo.entity.Token;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.OauthService;
import com.qc.casserver.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 原始CAS认证接口
 * CAS1.0
 */
@CrossOrigin("*")
@Slf4j
@Controller//@ResponseBody+@Controller
@RequestMapping("/cas-v1")
public class CASController {
    @Autowired
    private AuthService authService;

    /**
     * 返回字符串yesusername或者no
     * @param ticket
     * @param service
     * @return
     */
    @GetMapping("/validate")
    public String serverTicketTOUserInfo(String ticket,String service){
        if (ticket==null){
            return "no";
        }

        R<UserResult> userInfoByST = authService.getUserInfoByST(ticket,service);
        if (userInfoByST.getCode().equals(1)){
            return "yes\n"+userInfoByST.getData().getUsername();
        }
        return "no";
    }
    @GetMapping("/login")
    public void casLogin(HttpServletResponse response, String service) throws IOException {
        response.sendRedirect(service);
    }
}
