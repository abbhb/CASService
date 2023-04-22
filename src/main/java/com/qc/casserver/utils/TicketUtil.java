package com.qc.casserver.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;


/**
 * 定义Ticket
 */

public class TicketUtil {
    private static final String secret="reggit20230113.FfG!D3a2AcfrF.2u0C1";
    public static String charset = "utf-8";
    /**
     * 服务端使用没必要加密
     * 减小数据库压力，把权限也放入
     * @param username
     * @return
     */
    public static String addNewTGT(String username,Long userId,Integer permission){
        String message="TGC:"+username+"==="+ userId+"==="+permission;
        return message;
    }


    /**
     * 用于生成TGC（浏览器侧）
     * 过期时间均由redis来自动过期
     * @param username
     * @return
     */
    public static String addNewTGC(String userId,String username){
        String message="tk:"+userId+","+username+","+ LocalDateTime.now();
        Mac sha256_HMAC = null;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes("utf-8"), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hash = sha256_HMAC.doFinal(message.getBytes("utf-8"));
            String encodeStr = Base64.encodeBase64String(hash);
            String encodeStr16=byte2Hex(hash);
            return encodeStr16;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 通过TGT获取用户id
     * @param TGT
     * @return
     */
    public static String getUserIdByTGT(String TGT){
        String message= TGT.split("===")[1];
        return message;
    }

    /**
     * 用过TGT返回权限代码
     * @param TGT
     * @return
     */
    public static Integer getUserPermissionByTGT(String TGT){
        Integer permission = Integer.valueOf(TGT.split("===")[2]);
        return permission;
    }


    /**
     * 生成ST
     * @param username
     * @param userId
     * @param permission
     * @return
     */
    public static String addNewST(String username,Long userId,Integer permission) {
        String message="ST:"+username+"==="+ userId+"==="+permission;
        String s = null;
        try {
            s = RsaUtils.publicKeyEncrypt(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    /**
     * 获取st对应的UserID
     * @param st
     * @return
     */
    public static String getUserId(String st){
        String s = null;
        try {
            s = RsaUtils.privateKeyDecrypt(st);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return s.split("===")[1];
    }

    public static String AESdecode(String res) {
        return keyGeneratorES(res, "AES", secret, 128, false);
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 使用KeyGenerator双向加密，DES/AES，注意这里转化为字符串的时候是将2进制转为16进制格式的字符串，不是直接转，因为会出错
     * @param res 加密的原文
     * @param algorithm 加密使用的算法名称
     * @param key  加密的秘钥
     * @param keysize
     * @param isEncode
     * @return
     */
    private static String keyGeneratorES(String res,String algorithm,String key,int keysize,boolean isEncode){
        try {
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            if (keysize == 0) {
                byte[] keyBytes = charset==null?key.getBytes():key.getBytes(charset);
                kg.init(new SecureRandom(keyBytes));
            }else if (key==null) {
                kg.init(keysize);
            }else {
                byte[] keyBytes = charset==null?key.getBytes():key.getBytes(charset);
                kg.init(keysize, new SecureRandom(keyBytes));
            }
            SecretKey sk = kg.generateKey();
            SecretKeySpec sks = new SecretKeySpec(sk.getEncoded(), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            if (isEncode) {
                cipher.init(Cipher.ENCRYPT_MODE, sks);
                byte[] resBytes = charset==null?res.getBytes():res.getBytes(charset);
                return parseByte2HexStr(cipher.doFinal(resBytes));
            }else {
                cipher.init(Cipher.DECRYPT_MODE, sks);
                return new String(cipher.doFinal(parseHexStr2Byte(res)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }


    /**
     * 将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }



}
