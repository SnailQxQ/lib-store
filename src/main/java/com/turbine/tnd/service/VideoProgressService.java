package com.turbine.tnd.service;

import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.dao.ResourceDao;
import com.turbine.tnd.utils.VideoProgressUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author Turbine
 * @Description:    视频资源相关
 * @date 2023/6/24 10:23
 */
@Service
public class VideoProgressService {
    @Autowired
    ResourceDao rdao;

    public double getProgress(String fileId) {
        Resource resource = rdao.inquireByName(fileId);
        int end = resource.getLocation().lastIndexOf(File.separator);
        String dir = resource.getLocation().substring(0,end);
        System.out.println("开始获取流程-文件夹路径："+dir);

        String logPath = dir + File.separator + fileId + ".log";

        return VideoProgressUtils.getVideoProgress(logPath,resource.getLocation());
    }


    public static void main(String[] args) {
        VideoProgressService vs = new VideoProgressService();
        double progress = vs.getProgress("9dd428ea027aecba43c4296c09eeffcd");
        System.out.println(progress);
    }

}
