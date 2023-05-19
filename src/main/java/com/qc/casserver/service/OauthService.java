package com.qc.casserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Authorize;
import com.qc.casserver.pojo.entity.Oauth;
import com.qc.casserver.pojo.entity.PageData;

public interface OauthService extends IService<Oauth> {
    /**
     * 新建客户端
     * @param oauth
     * @return
     */
    Oauth addOauth(Oauth oauth);

    R<PageData<Oauth>> listOauth(Integer pageNum, Integer pageSize);

    R<String> delete(Long id);

    R<String> editAuth(Oauth oauth);

    R<UserResult> loginAggregationReturns(UserResult userResult, Authorize authorize);

}
