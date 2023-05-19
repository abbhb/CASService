package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.pojo.entity.Token;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.service.AuthService;
import com.qc.casserver.service.OauthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 原始CAS认证接口
 */
@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/v1")
public class CASController {
    @Autowired
    private AuthService authService;
    @GetMapping("/ticket")
    public R<UserResult> serverTicketTOUserInfo(String ticket){
        if (ticket==null){
            return R.error("访问被拒绝");
        }
        if (StringUtils.isEmpty(ticket)){
            return R.error("访问被拒绝");
        }
        return authService.getUserInfoByST(ticket);
    }
}
