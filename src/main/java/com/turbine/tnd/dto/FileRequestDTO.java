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
public class FileRequestDTO {
    //每个块的大小
    //@Value("${file.upload.sliceSize}")
    private Integer chunkSize;
    //当前块号
    private Integer chunkNum;
    //总块号
    private Integer totalChunkNum;
    //文件总大小
    private Long totalSize;
    //根据前端给的uuid文件名来标识是哪个文件
    private String fileName;
    //文件原始名
    private String originalName;
    //块文件内容
    private MultipartFile file;
    //上传者
    private String userName;
    //父文件id
    private Integer parentId;
    //缩略图
    private MultipartFile showImage;

}


