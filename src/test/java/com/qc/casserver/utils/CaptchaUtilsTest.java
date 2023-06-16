package com.qc.casserver.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaUtilsTest {

    @org.junit.jupiter.api.Test
    void getRandomChinese() throws IOException {
        String s = ImageUtil.GetBase64FromImage(CaptchaUtils.getBufferedImage(0));
        /* 写入Txt文件 */
        File writename = new File("E:\\web\\111\\img\\1.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件
        writename.createNewFile(); // 创建新文件
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));
        out.write(s); // \r\n即为换行
        out.flush(); // 把缓存区内容压入文件
        out.close(); // 最后记得关闭文件
        System.out.println(s);
    }
}