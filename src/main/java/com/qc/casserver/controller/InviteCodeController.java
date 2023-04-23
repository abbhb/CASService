package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.pojo.InviteCodeResult;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.InviteCodeService;
import com.qc.casserver.utils.TGTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/api2/invitecode")
public class InviteCodeController {
    private final InviteCodeService inviteCodeService;

    private final IRedisService iRedisService;

    @Autowired
    public InviteCodeController(InviteCodeService inviteCodeService, IRedisService iRedisService) {
        this.inviteCodeService = inviteCodeService;
        this.iRedisService = iRedisService;
    }

    @GetMapping("/listSelf")
    @NeedLogin
    public R<PageData<InviteCodeResult>> listMyInviteCode(Integer pageNum, Integer pageSize, HttpServletRequest request){
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        return inviteCodeService.listMyInviteCode(pageNum,pageSize,userId);
    }

}
