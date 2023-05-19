package com.qc.casserver.utils;

import com.qc.casserver.common.CustomException;
import com.qc.casserver.service.IRedisService;
import org.apache.commons.lang.StringUtils;

/**
 * oauth2.0工具类
 * 生成code
 * 消耗code
 */
public class OAuthUtil {
    public static String addNewCode(IRedisService iRedisService,Long userId) {
        //生成base64编码的40位随机字符串不包含符号
        String code = RandomName.getUUID();
        iRedisService.addAuthorizeCode(code,String.valueOf(userId));
        return code;
    }
    public static void deleteCode(IRedisService iRedisService,String code) {
        iRedisService.deleteAuthorizeCode(code);
    }
    public static Long getUserIdByCode(IRedisService iRedisService,String code){
        String userId = iRedisService.getAuthorizeCode(code);
        if (StringUtils.isEmpty(userId)) {
            throw new CustomException("认证失败");
        }
        return Long.valueOf(userId);
    }
}
