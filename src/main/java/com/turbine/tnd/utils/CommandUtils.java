package com.turbine.tnd.utils;

import jdk.internal.util.xml.impl.Input;
import jdk.internal.util.xml.impl.ReaderUTF8;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author Turbine
 * @Description: 执行本地指令命令
 * @date 2023/1/31 13:37
 */
@Slf4j
public class CommandUtils {


    public static Process execCommand(String... comand) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(comand);

        builder.redirectErrorStream(true);
        Process process = null;
        BufferedReader br = null;
        try {
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(br != null)br.close();
        }

        return process;
    }

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
    //ffmpeg -i 123.mp4 -c:v libx264 -c:a aac -strict -2 -f hls -hls_list_size 0 -hls_time 15  123_out.m3u8 -loglevel debug
    //-progress pro.log
    public static void processVideo(String path,String target){

        ProcessBuilder builder = new ProcessBuilder("ffmpeg","-i",path,"-c:v","libx264","-c:a","aac","-strict","-2"
                ,"-f","hls","-hls_list_size","0","-hls_time","1","-loglevel","debug",target);

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
        //ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 -i 01.mp4
        //String path = "C:\\迅雷下载\\Test\\01.mp4";
        ProcessBuilder builder = new ProcessBuilder("ffprobe","-v","error","-show_entries","format=duration"
                ,"-of","default=noprint_wrappers=1:nokey=1","-i",path);

        builder.redirectErrorStream(true);
        Process process = null;
        try {
            process = builder.start();
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
        try ( FileInputStream is = new FileInputStream(logFile);
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
        /*try ( FileInputStream is = new FileInputStream(file);
              BufferedReader reader = new BufferedReader(new InputStreamReader(is));){
              double progress = 0;
              while(progress != 100){
                  progress = calProgress(reader,allTime);
                  System.out.println( progress + "%" );
                  System.out.println("-=============================-");
              }

        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    public static void main(String[] args) throws Exception {
        /*String videoPath = "C:\\迅雷下载\\Test\\01.mp4";
        String logPath = "C:\\迅雷下载\\Test\\pro.log";
        Date date = new Date();
        long start = date.getTime();
        System.out.println(date.getTime());

        try {
            double progress = 0;
            while(progress != 100)
            getVideoProgress(logPath,videoPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Date d = new Date();
        long end = d.getTime();
        System.out.println(d.getTime() + " " + (end-start));*/

            System.out.println("发送端启动！");
            DatagramSocket Server = new DatagramSocket(new InetSocketAddress("127.0.0.1", 8888));
            String address = "127.0.0.1:9999";
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 6666);
            DatagramPacket packet = new DatagramPacket(address.getBytes(StandardCharsets.UTF_8),0,address.getBytes(StandardCharsets.UTF_8).length
            ,serverAddress);

            Server.send(packet);

    }
}
