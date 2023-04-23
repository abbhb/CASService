package com.qc.casserver.controller;


import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/common")

public class CommonController {
    @Autowired
    private CommonService commonService;
    @CrossOrigin("*")
    @NeedLogin
    @PermissionCheck("2")
    @PostMapping("/uploadimage")
    public R<String> uploadImage(MultipartFile file){
        return commonService.uploadFileTOMinio(file);

    }

    @PostMapping("/getcode")
    public R<String> getCode(String email){
        //此接口需加密，并且对用户限流
        return commonService.sendEmailCode(email);
    }


}
