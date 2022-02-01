package com.turbine.tnd.service;

import com.turbine.tnd.bean.ResourceType;

import java.io.File;

/**
 * @author Turbine
 * @Description 文件接口
 * @date 2022/1/24 21:02
 */
public interface FileService {
    //上传文件接口
    boolean uploadFile(File file);
    //下载文件
    File downLoadFile(String path);
    //文件是否支持
    boolean isSupport(String fileName);
}
