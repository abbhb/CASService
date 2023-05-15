package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.pojo.InviteCodeResult;
import com.qc.casserver.pojo.entity.invitecode.InviteCode;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.invitecode.ListInviteCode;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.InviteCodeService;
import com.qc.casserver.utils.TGTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


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
    @DeleteMapping("/deleteOne")
    @NeedLogin
    @PermissionCheck("1")
    public R<String> deleteInviteCodeOne(HttpServletRequest request,Long id){
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        if (userId==null||id==null){
            return R.error("无法删除");
        }
        boolean b = inviteCodeService.deleteOneInviteCode(id, userId);
        if (!b){
            return R.error("无法删除");
        }
        return R.success("删除成功");
    }

    @DeleteMapping("/deleteList")
    @NeedLogin
    @PermissionCheck("1")
    public R<String> deleteInviteCodeList(HttpServletRequest request,String id){
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        if (userId==null|| StringUtils.isEmpty(id)){
            return R.error("无法删除");
        }
        boolean b = inviteCodeService.deleteListInviteCode(id, userId);
        if (!b){
            return R.error("无法删除");
        }
        return R.success("删除成功");
    }

    @PostMapping("/addOne")
    @NeedLogin
    @PermissionCheck("1")
    public R<InviteCodeResult> addInviteCodeOne(@RequestBody InviteCode inviteCode,HttpServletRequest request){

        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        log.info("inviteCode={},userId={}",inviteCode,userId);
        return inviteCodeService.addInviteCode(userId,inviteCode);
    }
    @PostMapping("/addList")
    @NeedLogin
    @PermissionCheck("1")
    public R<List<InviteCodeResult>> addInviteCodeList(@RequestBody ListInviteCode listInviteCode,HttpServletRequest request){
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        return inviteCodeService.addInviteCodeList(userId,listInviteCode);
    }

    @GetMapping("/listSelf")
    @NeedLogin
    public R<PageData<InviteCodeResult>> listMyInviteCode(Integer pageNum, Integer pageSize, HttpServletRequest request){
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        return inviteCodeService.listMyInviteCode(pageNum,pageSize,userId);
    }

    @GetMapping("/listAll")
    @PermissionCheck("1")
    @NeedLogin
    public R<PageData<InviteCodeResult>> listAllInviteCode(Integer pageNum, Integer pageSize, HttpServletRequest request){
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        return inviteCodeService.listAllInviteCode(pageNum,pageSize,userId);
    }

}
