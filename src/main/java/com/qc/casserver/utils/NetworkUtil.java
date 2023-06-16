package com.qc.casserver.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtil {
    public static boolean network_check() {
        // 204 检测联网大法
        String Check_url = "https://connect.rom.miui.com/generate_204";
        try {
            URL url = new URL(Check_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(2000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(2000);
            // 发送请求
            connection.connect();
            return connection.getResponseCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
