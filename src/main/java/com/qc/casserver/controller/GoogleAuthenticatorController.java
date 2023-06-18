package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.pojo.entity.GoogleAuthenticator;
import com.qc.casserver.pojo.vo.GoogleAuthenticatorR;
import com.qc.casserver.service.GoogleAuthenticatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/google_auth")
public class GoogleAuthenticatorController {

    private final GoogleAuthenticatorService googleAuthenticatorService;


    public GoogleAuthenticatorController(GoogleAuthenticatorService googleAuthenticatorService) {
        this.googleAuthenticatorService = googleAuthenticatorService;
    }

    @GetMapping("/change_state")
    @NeedLogin
    public R<String> changeState(Integer state) {
        if (state == null) {
            return R.error("异常");
        }
        return googleAuthenticatorService.changeState(state);
    }

    @GetMapping("/get_google_authenticator")
    @NeedLogin
    public R<GoogleAuthenticatorR> getGoogleAuthenticator() {
        return googleAuthenticatorService.getSecret();
    }

    @PostMapping("/verify")
    @NeedLogin
    public R<GoogleAuthenticatorR> verify(@RequestBody GoogleAuthenticator googleAuthenticator, @RequestParam("code") String code) {
        if (googleAuthenticator == null) {
            return R.error("请保证参数正常");
        }
        return googleAuthenticatorService.verify(googleAuthenticator, Long.valueOf(code));
    }
}
