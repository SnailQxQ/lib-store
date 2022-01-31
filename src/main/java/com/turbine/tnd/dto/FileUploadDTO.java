package com.turbine.tnd.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Turbine
 * @Description 返回给前端文件上传情况的dto
 * @date 2022/1/26 19:15
 */
@Data
public class FileUploadDTO {
    //已经传输完成的块号
    int chunkNum;
    //当前块传输是否完成
    boolean accomplish;
    //是否所有块都传输完成
    boolean allSuccess;

}
