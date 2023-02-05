package com.turbine.tnd.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/19 16:00
 */
public class MD5Util {
    private static final String salt = "--==++Turbine--==++";

    //直接根据内容加密
    public static String enryption(String content){
        return DigestUtils.md5Hex(content);
    }

    //盐值加密
    public static String saltEnryption(String content){
        return DigestUtils.md5Hex((salt+content).getBytes()) ;
    }

    /**
     *
     * @param target    待验证的串
     * @param md5       md5密文串
     * @return
     */
    public static boolean verification(String target,String md5,boolean useSalt){
        String verify = "";
        if(useSalt) verify =  saltEnryption(target);
        else verify = enryption(target);

        return md5.equals(verify);
    }

    //生成从ASCII 32到126组成的随机字符串 （包括符号）作为盐值
    public static String randomSaltEnryption(String content) {
       return  enryption( RandomStringUtils.randomAscii(8)+content);
    }

    //需要key的加密
    public static String enryptionByKey(String text, String key) {
        return  enryption(text+key);
    }

    public static void main(String[] args) {
        System.out.println(saltEnryption("1234567"));
        System.out.println(saltEnryption("990508"));
        System.out.println(-Integer.MIN_VALUE);
        int c  = 0x1;
    }
}
