package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.service.VideoProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * @author Turbine
 * @Description:
 * @date 2023/6/23 17:27
 */

@ServerEndpoint("/resource/socket")
@Component
public class VideoProgressSocketController {
    private String fileId;

    private static VideoProgressService videoService;

    //scoket 是多对象，而spring 默认是单例，如果不改为静态，spring只会注入一次videoService，后面新建的链接对象socket会拿不到该service
    @Autowired
    public void setVideoProgressService(VideoProgressService videoService){
        this.videoService = videoService;
    }
    //TODO:MP4和ts 是属于同一级
    @OnOpen
    public void onOpen(Session session ) throws InterruptedException {
        System.out.println("后端建立链接成功！获取转码文件："+fileId);
        sendMessage("我是后端发送的数据！",session);
    }


    @OnMessage
    public void onMessage(String message, Session session) throws InterruptedException {
        this.fileId = message;
        double data = 0.0;
        while(data != 100){
            data = videoService.getProgress(fileId);
            System.out.println("准备发送最新进度："+data);
            sendMessage("视频转码进度！:"+data,session);
            Thread.sleep(800);
        }
    }


    @OnClose
    public void onClose(Session session){
        System.out.println("链接关闭");
    }

    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }


    public static void sendMessage(String message, Session session){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
