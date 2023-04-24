package com.qc.casserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "login")
public class LoginConfig {
    private boolean needCaptcha;//是否校验需要验证码
}
