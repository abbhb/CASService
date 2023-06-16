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
        /* д��Txt�ļ� */
        File writename = new File("E:\\web\\111\\img\\1.txt"); // ���·�������û����Ҫ����һ���µ�output��txt�ļ�
        writename.createNewFile(); // �������ļ�
        BufferedWriter out = new BufferedWriter(new FileWriter(writename));
        out.write(s); // \r\n��Ϊ����
        out.flush(); // �ѻ���������ѹ���ļ�
        out.close(); // ���ǵùر��ļ�
        System.out.println(s);
    }
}