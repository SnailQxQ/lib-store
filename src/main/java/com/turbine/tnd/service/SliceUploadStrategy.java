package com.turbine.tnd.service;

import com.turbine.tnd.dto.FileUploadDTO;
import com.turbine.tnd.dto.FileUploadRequestDTO;
import org.springframework.stereotype.Service;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/26 18:58
 */
@Service("SliceUploadStrategy")
public interface SliceUploadStrategy {
    //实现分片上传功能
    FileUploadDTO sliceUpload(FileUploadRequestDTO param);


}
