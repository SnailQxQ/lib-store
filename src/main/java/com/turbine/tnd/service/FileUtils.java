package com.turbine.tnd.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/26 21:00
 */
@Slf4j
public class FileUtils {

    public static String getPath(String dir){
        LocalDateTime date = LocalDateTime.now();
        return dir+"/"+date.getYear()+"/"+date.getMonth()+"/"+date.getDayOfMonth();
    }
    //获取的路径格式没有天
    public static String getPathNoneDay(String dir){
        LocalDateTime date = LocalDateTime.now();
        return dir+"/"+date.getYear()+"/"+date.getMonth();
    }

    /**
     * 从配置文件中读出字节数组
     * @param configFile
     * @return
     */
    public static byte[] readFileToByteArry(RandomAccessFile configFile) {
        byte[] bytes = null;
        try {

            log.debug("conf File size :"+configFile.length());
            int size = Integer.parseInt(configFile.length()+"");
            configFile.seek(0);
             bytes = new byte[(int)size];
            configFile.readFully(bytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }

    /**
     * 根据文件内容获取MD5
     * @param file
     * @return
     *  第一步获取文件的byte信息，
     *  第二步通过MessageDigest类进行MD5加密，
     *  第三步转换成16进制的MD5码值
     */

    //根据文件获取文件MD5
    public static String getFileMD5(File file){
        String s = null;

        try {
            s = getFileMD5( new FileInputStream(file) );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return s;
    }

    //根据文件流获取文件MD5
    public static String getFileMD5(FileInputStream fis){
        String s = null;
        try {
            s = DigestUtils.md5Hex(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;
    }

    //根据输入流获取文件MD5
    public static String getFileMD5(InputStream fis){
        String s = null;
        try {
            s = DigestUtils.md5Hex(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;
    }

}
