package com.qc.casserver.controller;


import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.config.LoginConfig;
import com.qc.casserver.pojo.EmailCode;
import com.qc.casserver.pojo.entity.Captcha;
import com.qc.casserver.service.CaptchaService;
import com.qc.casserver.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/common")

public class CommonController {
    @Autowired
    private CommonService commonService;

    @Autowired
    private LoginConfig loginConfig;

    @Autowired
    private CaptchaService captchaService;
    @CrossOrigin("*")
    @NeedLogin
    @PermissionCheck("2")
    @PostMapping("/uploadimage")
    public R<String> uploadImage(MultipartFile file){
        return commonService.uploadFileTOMinio(file);

    }


    @PostMapping("/getEmailCode")
    public R<String> getCode(@RequestBody EmailCode emailCode){
        if (emailCode==null){
            return R.error("发送失败");
        }
        //此接口需加密，并且对用户限流
        return commonService.sendEmailCode(emailCode);
    }

    @PostMapping("/havaEmailCode")
    public R<Integer> havaEmailCode(@RequestBody EmailCode emailCode){
        //若调用此接口1天内超过100以上次封号处理，保护安全（依托限流）
        //5次以上直接不告诉是否正确,后期限流
        if (emailCode==null){
            return R.error("校验失败");
        }
        //此接口需加密，并且对用户限流
        return commonService.havaEmailCode(emailCode);
    }
    @NeedLogin
    @GetMapping("/getImage")
    public R<String> getImageTrueUrl(String id){
        return R.success(new String(commonService.getFileFromMinio(id)));
    }

    /**
     * 获取验证码拼图
     * @param captcha
     * @return
     */
    @CrossOrigin("*")
    @PostMapping("/getCaptcha")
    public R<Captcha> getCaptcha(@RequestBody Captcha captcha) {
        return R.success(captchaService.getCaptcha(captcha));
    }

    /**
     * 1为需要，
     * 0为不需要
     * @return
     */
    @CrossOrigin("*")
    @GetMapping("/getIfNeedCaptcha")
    public R<Integer> getIfNeedCaptcha(){
        if (loginConfig.isNeedCaptcha()){
            return R.success(1);
        }
        return R.success(0);
    }
    @CrossOrigin("*")
    @PostMapping("/checkImageCode")
    public R<Captcha> checkImageCode(@RequestBody Captcha captcha) {
        String s = captchaService.checkImageCode(captcha.getNonceStr(), captcha.getValue());
        if (!StringUtils.isEmpty(s)){
            return R.error(s);
        }
        return R.successOnlyMsg("校验成功");
    }



}
