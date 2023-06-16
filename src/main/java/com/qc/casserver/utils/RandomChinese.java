package com.qc.casserver.utils;

import java.util.Random;

public class RandomChinese {
    // 生成指定范围内的随机整数
    public static int getRandomInt(int min, int max) {
        return new Random().nextInt(max - min) + min;
    }

    // 生成随机汉字
    public static String getRandomChinese() {
        int randomInt = getRandomInt(0x4e00, 0x9fa5);
        return String.valueOf(Character.toChars(randomInt));
    }

    // 生成随机汉字
    public static String getRandomChineseLen(int m) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < m; i++) {
            str.append(getRandomChinese());
        }
        return str.toString();
    }

}