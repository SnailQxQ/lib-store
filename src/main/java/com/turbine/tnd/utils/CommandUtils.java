package com.turbine.tnd.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Turbine
 * @Description: 执行本地指令命令
 * @date 2023/1/31 13:37
 */
@Slf4j
public class CommandUtils {

    /**
     * @Description:  调用本地ffmpeg 命令生成m3u8 文件与 ts文件
     * @author Turbine
     * @param
     * @param path      原文件目录
     * @param target    目标文件目录
     * @return void
     * @date 2023/1/31 13:40
     */
    /*
        -hls_time n: 设置每片的长度，默认值为2。单位为秒
        -hls_list_size n:设置播放列表保存的最多条目，设置为0会保存有所片信息，默认值为5
        -hls_wrap n:设置多少片之后开始覆盖，如果设置为0则不会覆盖，默认值为0.这个选项能够避免在磁盘上存储过多的片，而且能够限制写入磁盘的最多的片的数量
        -hls_start_number n:设置播放列表中sequence number的值为number，默认值为0
     */
    //ffmpeg -i 123.mp4 -c:v libx264 -c:a aac -strict -2 -f hls -hls_list_size 2 -hls_time 15  123_out.m3u8 -loglevel debug
    public static void processVideo(String path,String target){

        ProcessBuilder builder = new ProcessBuilder("ffmpeg","-i",path,"-c:v","libx264","-c:a","aac","-strict","-2"
                ,"-f","hls","-hls_list_size","5","-hls_time","5","-loglevel","debug",target);

        builder.redirectErrorStream(true);
        Process process = null;
        if(! new File(path).exists() )log.debug(" processVideo 文件不存在 ："+path);
        try {
            process = builder.start();
            BufferedReader br = new BufferedReader( new InputStreamReader(process.getInputStream()) );
            String line;
            while((line = br.readLine()) != null){
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
