package com.qc.casserver.common;

public class MyString {
    /**
     * 此配置当项目运行后不要轻易改变
     */
    public final static String pre_phone_redis = "number_code:";

    public final static String pre_email_redis = "emailcode:";

    public final static String pre_user_redis = "user:";

    public final static String ST_PRE = "st:";

    public final static String permission_key = "cas:permission";

    /**
     * 限流
     */
    public final static String LIMITING_NameSpaces = "LIMITING::";

    /**
     * 按用户进行限流，后面接userID
     * 用户具体某个接口限流
     * LIMITING::USER::1::Controller
     */
    public final static String LIMITING_NameSpaces_USER = LIMITING_NameSpaces+"USER::";


    public static Object pre_access_token = "access_token::";

    public static Object pre_refresh_token = "refresh_token::";
}
