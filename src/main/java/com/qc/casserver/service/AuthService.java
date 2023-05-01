package com.qc.casserver.service;

import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Token;

public interface AuthService {
    R<Token> addToken(String st);

    R<UserResult> getUserByAccessToken(String accessToken);

    R<Token> refreshToken(String refreshToken);

    R<String> logoutToken(Token token);
}
