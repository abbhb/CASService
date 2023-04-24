package com.qc.casserver.controller;


import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
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
    private CaptchaService captchaService;
    @CrossOrigin("*")
    @NeedLogin
    @PermissionCheck("2")
    @PostMapping("/uploadimage")
    public R<String> uploadImage(MultipartFile file){
        return commonService.uploadFileTOMinio(file);

    }

    @GetMapping("/getCode")
    public R<String> getCode(String email){
        //此接口需加密，并且对用户限流
        return commonService.sendEmailCode(email);
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
    @CrossOrigin("*")
    @PostMapping("/checkImageCode")
    public R<Captcha> checkImageCode(@RequestBody Captcha captcha) {
        String s = captchaService.checkImageCode(captcha.getNonceStr(), captcha.getValue());
        if (!StringUtils.isEmpty(s)){
            return R.error(s);
        }
        return R.success("校验成功");
    }



}
