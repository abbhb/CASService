package com.qc.casserver.common;

public class MyString {
    /**
     * 此配置当项目运行后不要轻易改变
     */
    /**
     * 总前缀
     */
    public final static String pre_all = "CAS::";
    public final static String pre_phone_redis = pre_all+ "number_code:";

    public final static String pre_email_redis = pre_all+ "emailcode:";

    public final static String pre_user_redis =pre_all+  "user:";

    public final static String ST_PRE = pre_all+ "st:";

    public final static String permission_key = pre_all+ "cas:permission";


    public static Object pre_tgc = pre_all+  "tgc_tgt::";

    public static Object pre_access_token = pre_all+  "access_token::";

    public static Object pre_refresh_token = pre_all+  "refresh_token::";


    /**
     * 限流
     */
    public final static String LIMITING_NameSpaces = pre_all+ "LIMITING::";

    /**
     * 按用户进行限流，后面接userID
     * 用户具体某个接口限流
     * LIMITING::USER::1::Controller
     */
    public final static String LIMITING_NameSpaces_USER = LIMITING_NameSpaces+"USER::";
}
