package com.qc.casserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.entity.GoogleAuthenticator;
import com.qc.casserver.pojo.vo.GoogleAuthenticatorR;

public interface GoogleAuthenticatorService extends IService<GoogleAuthenticator> {
    R<String> changeState(Integer state);

    R<GoogleAuthenticatorR> getSecret();

    R<GoogleAuthenticatorR> verify(GoogleAuthenticator googleAuthenticator, Long code);
}
