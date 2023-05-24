package com.qc.casserver.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Slf4j
public class JWTUtil {
    private final static String secret_key = "react20230513.FfG!D3a2AcfrF.2u0C1";
    /**
     * 生成加密后的st
     * @return 加密后的token
     */
    public static String getToken(String tgt, String service) {
        String st = null;
        try {//60L *
//            Date expiresAt = new Date(System.currentTimeMillis()+ 1L*60L * 1000L);//token 60分钟内必须刷新，后期加个刷新令牌，刷新令牌放redis里
            //转换成base64
            service = Base64Util.encode(service);
            log.info("11111{}",service);

            st = JWT.create().withIssuer(service).withClaim("tgt", tgt)
                    // 使用了HMAC256加密算法。
                    // mysecret是用来加密数字签名的密钥。
                    .sign(Algorithm.HMAC256(secret_key));
        } catch (JWTCreationException exception) {
            // Invalid Signing configuration / Couldn't convert Claims.
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String s = Base64.getEncoder().encodeToString(st.getBytes());
        return s;
    }


    public static String getTGT(String token,String service){
        String tgts = null;

        DecodedJWT decodedJWT = JWTUtil.deToken(token,service);
        Claim tgt = decodedJWT.getClaim("tgt");
        if (StringUtils.isNotEmpty(tgt.asString())){
            tgts = tgt.asString();
        }
        return tgts;
    }
    /**
     * 解密
     * @author 张超
     *
     */
    /**
     * 先验证token是否被伪造，然后解码token。
     * @param token 字符串token
     * @return 解密后的DecodedJWT对象，可以读取token中的数据。
     */
    public static DecodedJWT deToken(String token,String service) {
        byte[] decode = Base64.getDecoder().decode(token);
        token = new String(decode);
        DecodedJWT jwt = null;

        // 使用了HMAC256加密算法。
        // mysecret是用来加密数字签名的密钥。
        //转换成base64
        service = Base64Util.encode(service);
        log.info("22222-{}",service);

        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret_key)).withIssuer(service).build().verify(token);// Reusable
// verifier
        // instance

        return decodedJWT;
    }

}
