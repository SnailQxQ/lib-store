package com.turbine.tnd.utils;

import jdk.internal.util.xml.impl.Input;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Turbine
 * @Description:
 * @date 2023/6/20 22:31
 */
@Slf4j
public class VideoProgressUtils {
    /*
        -hls_time n: 设置每片的长度，默认值为2。单位为秒
        -hls_list_size n:设置播放列表保存的最多条目，设置为0会保存有所片信息，默认值为5
        -hls_wrap n:设置多少片之后开始覆盖，如果设置为0则不会覆盖，默认值为0.这个选项能够避免在磁盘上存储过多的片，而且能够限制写入磁盘的最多的片的数量
        -hls_start_number n:设置播放列表中sequence number的值为number，默认值为0
     */
    //ffmpeg -i 123.mp4 -c:v libx264 -c:a aac -strict -2 -f hls -hls_list_size 0 -hls_time 15  123_out.m3u8 -loglevel debug
    //-progress pro.log
    /**
     * @Description:
     * @author Turbine
     * @param
     * @param path      视频文件地址
     * @param target    输出转码文件地址
     * @param logPath   转码文件日志地址
     * @return void
     * @date 2023/6/20 22:36
     */
    public static void processVideo(String path,String target,String logPath){
        String[] command = {"ffmpeg","-i",path
                ,"-c:v","libx264"
                ,"-c:a","aac"
                ,"-strict","-2"
                ,"-f","hls"
                ,"-hls_list_size","0"
                ,"-hls_time","1"
                ,"-loglevel","debug",target
                ,"-progress",logPath};
        if(! new File(path).exists() ){
            log.debug(" processVideo 文件不存在 ："+path);
            return;
        }
        try {
            Process process = CommandUtils.execCommand(command);
            if(process != null){
                BufferedReader br = new BufferedReader( new InputStreamReader(process.getInputStream()) );
                String line;
                while((line = br.readLine()) != null){
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            log.debug(path+" 处理完毕！");
        }
    }

    /**
     * @Description:
     * @author Turbine
     * @param
     * @param path 视频资源文件路径
     * @return double
     * @date 2023/6/18 19:19
     */
    private static double getAllTime(String path){
        double result = 0;
        String[] command = {"ffprobe","-v","error","-show_entries","format=duration","-of","default=noprint_wrappers=1:nokey=1","-i",path};

        Process process = null;
        try {
            process = CommandUtils.execCommand(command);
            BufferedReader br = new BufferedReader( new InputStreamReader(process.getInputStream()) );
            String line = br.readLine();
            if(line != null && line.indexOf("erro") == -1)result = Double.valueOf(line);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            log.debug("==========时间长度 "+result+" 秒！========================");
        }
        return result;
    }

    //hh:mm:ss => seconds
    public static double transferTime(String time){
        String[] split = time.split(":");
        return Double.parseDouble(split[0])*3600  + Double.parseDouble(split[1])*60 + Double.parseDouble(split[2]);
    }

    /**
     * @Description:
     * @author Turbine
     * @param
     * @param logFile       ffmepg 日志文件
     * @param allTime       视频总时常
     * @return double
     * @date 2023/6/18 19:18
     */
    public static double calProgress(File logFile,double allTime) throws IOException {
        double progress = 0.0;
        String line;
        try (FileInputStream is = new FileInputStream(logFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));){

            while((line = reader.readLine()) != null){
                int i = line.lastIndexOf("out_time=");
                if(i > -1){
                    String nowTime = line.substring(i+9,i+9+8);
                    progress = (transferTime(nowTime)/allTime)*100;
                    if(progress > 99.5)progress = 100;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return progress;
    }

    /**
     * @Description:
     * @author Turbine
     * @param
     * @param LogPath  日志文件路径
     * @param videoPath 视频文件路径
     * @return void
     * @date 2023/6/18 18:15
     */
    public static void getVideoProgress(String LogPath,String videoPath) throws IOException, InterruptedException {
        double allTime = getAllTime(videoPath);
        File file = new File(LogPath);
        int i = 1;

        try {
            if(!file.exists()){
                //重试三次
                while(i <= 3 ){
                    if(file.exists()){
                        break;
                    }
                    System.out.println("重试第"+i+"次");
                    Thread.sleep(2000);
                    i++;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(!file.exists() && i == 4){
            log.debug("获取解码文件重试失败！");
            return;
        }
        double progress = 0;
        while(progress != 100){
            progress = calProgress(file,allTime);
            System.out.println( progress + "%" );
            System.out.println("-=============================-");
            Thread.sleep(500);
        }
    }


    public static void main(String[] args) throws Exception {
        System.out.println("接收端启动！");
        DatagramSocket Server = new DatagramSocket(new InetSocketAddress("127.0.0.1", 6666));
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data,0,data.length);
        Server.receive(packet);

        String rec = new String(packet.getData(), 0, packet.getLength());

        System.out.println(rec);
        InetAddress inetAddress = Server.getInetAddress();
        System.out.println(inetAddress.toString());
    }


}
