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
    //当前传输的块号块号
    Integer chunkNum;
    //当前块传输是否完成
    Boolean accomplish;
    //是否所有块都传输完成
    Boolean allSuccess;
    //上传完成返回文件对象信息
    ResourceDTO resource;

}
