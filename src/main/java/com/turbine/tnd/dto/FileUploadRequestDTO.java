package com.turbine.tnd.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author Turbine
 * @Description 实现分片传输的dto
 * @date 2022/1/26 19:00
 * //临时文件的目录后台来生成
 */
@Data
@ToString
public class FileUploadRequestDTO {
    //每个块的大小
    //@Value("${file.upload.sliceSize}")
    private int sliceSize;
    //当前块号
    int chuckNum;
    //总块号
    int totalChuckNum;
    //文件总大小
    int totalSize;
    //根据前端给的uuid文件名来标识是哪个文件
    String fileName;
    //块文件内容
    MultipartFile file;
    //上传者
    String userName;


}


