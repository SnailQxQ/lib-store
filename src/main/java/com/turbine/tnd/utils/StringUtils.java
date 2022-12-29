package com.turbine.tnd.utils;

import java.util.Random;

/**
 * @author Turbine
 * @Description    字符串工具类
 * @date 2022/1/28 18:22
 */
public class StringUtils {

    public static String removeSurplus(String str) {
        return null;
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

}
