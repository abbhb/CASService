package com.qc.casserver.service;

import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Authorize;
import com.qc.casserver.pojo.entity.Token;
import com.qc.casserver.pojo.entity.User;

public interface AuthService {

    R<Token> addToken(Authorize authorize);

    R<UserResult> getUserByAccessToken(String accessToken);

    R<Token> refreshToken(String refreshToken);

    R<String> logoutToken(Token token);

    /**
     * 用于传统CAS认证
     * @param st
     * @return
     */
    R<UserResult> getUserInfoByST(String st,String service);
}
