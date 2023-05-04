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
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new CustomException("好奇怪，出错了");
        }
        String tgc = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("tgc")) {
                tgc = cookie.getValue();
                break;
            }
        }
        if (StringUtils.isEmpty(tgc)){
            throw new CustomException("出错了");
        }
        String tgt = iRedisService.getTGC(tgc);
        if (StringUtils.isEmpty(tgt)){
            throw new CustomException("需要认证");
        }
        String userIdByTGT = TicketUtil.getUserIdByTGT(tgt);
        return Long.valueOf(userIdByTGT);
    }

    public static String getTGCInRequest(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new CustomException("好奇怪，出错了");
        }
        String tgc = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("tgc")) {
                tgc = cookie.getValue();
                break;
            }
        }
        return tgc;
    }
}
