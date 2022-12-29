package com.turbine.tnd.service;

import com.turbine.tnd.dto.FileDownLoadDTO;
import com.turbine.tnd.dto.FileUploadDTO;
import com.turbine.tnd.dto.FileRequestDTO;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/26 18:58
 */
@Service("SliceUploadStrategy")
public interface SliceFileService {
    //实现分片上传功能
    FileUploadDTO sliceUpload(FileRequestDTO param);
    //分片下载
    FileDownLoadDTO sliceDownload(FileRequestDTO param);

    //检查已完成上传的块
    List<Integer> checkFinished(FileRequestDTO param);
    //检查文件类型是否支持
    boolean isSupport(String originalName);
    //删除当前用户上传的文件的所有临时文件 需要文件名和上传用户名
    void deleteTempFile(FileRequestDTO param);
}
