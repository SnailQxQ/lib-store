package com.turbine.tnd.dto;

import com.turbine.tnd.bean.UserResource;
import lombok.Data;

import java.sql.Date;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/8 12:13
 */
@Data
public class ResourceDTO {
    //用户资源id
    private Integer id;
    //资源名
    private String fileName;
    //是否共享
    private boolean isShare;
    //是否加密
    private boolean isEncrypt;
    //创建时间
    private Date createTime;
    //文件类型
    private String fileType;
    //文件MD5标识
    private String fileId;
    //文件大小
    private Long fileSize;

    public ResourceDTO(){};

    public ResourceDTO(UserResource ur) {
        if(ur.getId() != null)this.id = ur.getId();
        if(ur.getOriginalName() != null)this.fileName = ur.getOriginalName();
        if(ur.getS_flag() != null)this.isShare = ur.getS_flag();
        if(ur.getEncryption() != null)this.isEncrypt = ur.getEncryption();
        if(ur.getUploadTime() != null)this.createTime = ur.getUploadTime();
        if(ur.getFileName() != null)this.fileId = ur.getFileName();
    }
}
