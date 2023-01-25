package com.turbine.tnd.utils;

import com.turbine.tnd.bean.Folder;
import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.bean.UserResource;
import com.turbine.tnd.dto.ResourceDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Turbine
 * @Description 文件压缩工具类，支持对文件集合 或对文件夹进行压缩处理
 *              空文件夹会舍弃
 * @date 2023/1/12 15:08
 */
@Slf4j
public class ZipUtils {

    private  ZipUtils(){}



    /*
     * @Description: 对指定路径文件或文件夹进行压缩
     * @author Turbine
     * @param compresspath
     * @return boolean
     * @date 2023/1/12 17:01
     */
    public static boolean compressZip(String compresspath) {
        boolean bool = false;
        try {
            ZipOutputStream zipOutput = null;
            File file = new File(compresspath);
            if(file.isDirectory()){
                zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(compresspath + ".zip")));
                compress(zipOutput, file, ""); //递归压缩文件夹，最后一个参数传""压缩包就不会有当前文件夹；传file.getName(),则有当前文件夹;
            }else{
                zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(compresspath.substring(0, compresspath.lastIndexOf(".")) + ".zip")));
                zip(zipOutput,file,file.getName());//压缩单个文件
            }
            zipOutput.closeEntry();
            zipOutput.close();

            int read = 0 ;
            byte[] data = new byte[1024];
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**
     * @Description: 子文件夹中可能还有文件夹，进行递归
     * @param @param zipOutput
     * @param @param file
     * @param @param suffixpath
     * @param @throws IOException
     * @return void    返回类型
     * @throws
     */
    private static void compress(ZipOutputStream zipOutput, File file, String dir) {
        File[] listFiles = file.listFiles();// 列出所有的文件
        for(File fi : listFiles){
            if(fi.isDirectory()){
                if(dir.equals("")){
                    //第一级文件夹
                    compress(zipOutput, fi, fi.getName());
                }else{
                    compress(zipOutput, fi, dir + File.separator + fi.getName());//文件目录结构
                }
            }else{
                zip(zipOutput, fi, dir);
            }
        }
    }


    /**
     * @Description: 压缩文件或文件夹
     * @author Turbine
     * @param zipOutput
     * @param file
     * @param dir
     * @return void
     * @date 2023/1/12 16:50
     */
    public static void zip(ZipOutputStream zipOutput, File file, String dir) {
        try {
            ZipEntry zEntry = null;
            if(dir.equals("")){
                zEntry = new ZipEntry(file.getName());
            }else{
                //保存文件目录
                zEntry = new ZipEntry(dir + File.separator + file.getName());
            }
            zipOutput.putNextEntry(zEntry);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int read = 0;
            while((read = bis.read(buffer)) != -1){
                zipOutput.write(buffer, 0, read);
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 将文件列表压缩到指定的流中
     * @param files
     * @param os
     */
    public static boolean compressZip(List<File> files, OutputStream os){
        boolean result = false;

        try {
            //关闭外层流会将内层流也进行关闭，直接抛给最外层方法进行关闭
            ZipOutputStream zipOutStream = new ZipOutputStream(new BufferedOutputStream(os));
            int i=0;
            zipOutStream.setMethod(ZipOutputStream.DEFLATED);
            for(File file : files){
                FileInputStream fis = new FileInputStream(file);
                //防止同名文件压缩失败
                zipOutStream.putNextEntry(new ZipEntry(i+"_"+file.getName()));

                byte[] temp = new byte[1024];
                BufferedInputStream bis = new BufferedInputStream(fis);
                int read = 0;

                while((read = bis.read(temp) )!= -1){
                    zipOutStream.write(temp,0,read);
                }

                bis.close();
                zipOutStream.closeEntry();
                fis.close();
                log.debug("文件压缩完成");
                i++;
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();

        }


        return result;
    }

    /**
     * @Description:    将列表中的文件压缩至指定目录
     * @author Turbine
     * @param files     文件列表
     * @param zOutput
     * @param dir       指定目录
     * @return boolean
     * @date 2023/1/13 10:57
     */
    public static boolean compressZip(List<File> files,ZipOutputStream zOutput,String dir){
        boolean result = false;
        try {
            //关闭外层流会将内层流也进行关闭，直接抛给最外层方法进行关闭
            int i=0;
            zOutput.setMethod(ZipOutputStream.DEFLATED);
            for(File file : files){
                FileInputStream fis = new FileInputStream(file);
                //防止同名文件压缩失败
                zOutput.putNextEntry(new ZipEntry(dir+File.separator+i+"_"+file.getName()));

                byte[] temp = new byte[1024];
                BufferedInputStream bis = new BufferedInputStream(fis);
                int read = 0;

                while((read = bis.read(temp) )!= -1){
                    zOutput.write(temp,0,read);
                }

                bis.close();
                zOutput.closeEntry();
                fis.close();
                log.debug("文件压缩完成 dir:"+dir);
                i++;
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();

        }

        return result;
    }


    public static void main(String[] args) throws IOException {
        List<File> files = new ArrayList<>();
        File file1 = new File("C:\\Users\\turbine\\Desktop\\tnd.sql");
        File file2 = new File("C:\\Users\\turbine\\Desktop\\新建文本文档.txt");
        File f = new File("C:\\Users\\turbine\\Desktop\\newZip.zip");
        files.add(file1);
        files.add(file2);

        boolean newFile = f.createNewFile();

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(f));


        ZipEntry z2 = new ZipEntry(""+file1.getName());
        zipOutputStream.putNextEntry(z2);

        FileInputStream is = new FileInputStream(file1);
        byte[] d = new byte[1024];
        int read = 0;
        byte[] data = new byte[1024];
        while((read = is.read(data)) != -1){
            zipOutputStream.write(data,0,read);
        }
        zipOutputStream.closeEntry();
        //二级目录
        ZipEntry zipEntry1 = new ZipEntry(File.separator+"1"+File.separator+"2"+File.separator+file2.getName());
        zipOutputStream.putNextEntry(zipEntry1);

        zipOutputStream.closeEntry();

        ZipEntry zipEntry2 = new ZipEntry(File.separator + "1" + File.separator + file2.getName());
        zipOutputStream.putNextEntry(zipEntry2);
        read = 0;
        FileInputStream is2 = new FileInputStream(file2);

        while((read = is2.read(data))  != -1){
            zipOutputStream.write(data,0,read);
        }


        zipOutputStream.closeEntry();
        zipOutputStream.close();
        is.close();
        //is2.close();
    }
}
