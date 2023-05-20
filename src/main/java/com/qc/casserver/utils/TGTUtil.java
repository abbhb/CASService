package com.qc.casserver.utils;

import com.qc.casserver.common.CustomException;
import com.qc.casserver.service.IRedisService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Component
public class TGTUtil {

    public static Long getUserIdByTGTInRequest(HttpServletRequest request, IRedisService iRedisService){
        String tgc = request.getHeader("tgc");
        if (StringUtils.isEmpty(tgc)){
            throw new CustomException("出错了");
        }
        String tgt = iRedisService.getTGT(tgc);
        if (StringUtils.isEmpty(tgt)){
            throw new CustomException("需要认证");
        }
        String userIdByTGT = TicketUtil.getUserIdByTGT(tgt);
        return Long.valueOf(userIdByTGT);
    }

    public static String getTGCInRequest(HttpServletRequest request){
        String tgc = request.getHeader("tgc");

        return tgc;
    }
}
