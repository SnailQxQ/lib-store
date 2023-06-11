package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

import java.sql.Date;

/**
 * @author Turbine
 * @Description 用户资源Bean
 * @date 2022/1/29 15:35
 */
@Data
@ToString
public class UserResource {
    //用户资源id
    private Integer id;
    //上传用户id
    private Integer u_id;
    //资源id
    private Integer resourceId;
    //文件名 MD5资源标识
    private String fileName;
    //上传时间
    private Date uploadTime;
    //原文件名
    private String originalName;
    //加密密码
    private String encryptPsw;
    //是否加密
    private Boolean encryption = false;
    //删除标记
    private Boolean deleteFlag = false;
    //是否公开资源
    private Boolean shareFlag = false;
    //父文件id
    private Integer parentId;
    //类型id
    private Integer typeId;
    //资源预览图 base64
    private String thumbnail;
    //是否为收藏资源
    private Boolean collect = false;

    public UserResource(){}

    public UserResource(int userId, int resourceId,String fileName, String originalName, int parentId, int type_id) {
        this.u_id = userId;
        this.resourceId = resourceId;
        this.fileName = fileName;
        this.originalName = originalName;
        this.parentId = parentId;
        this.typeId = type_id;
    }

    public void assemble(UserResource uResource) {
        if(uResource.getU_id() != null)this.u_id = uResource.getU_id();
        if(uResource.getFileName() != null)this.fileName = uResource.getFileName();
        if(uResource.getUploadTime() != null)this.uploadTime = uResource.getUploadTime();
        if(uResource.getOriginalName() != null)this.originalName = uResource.getOriginalName();
        if(uResource.getEncryptPsw() != null)this.encryptPsw = uResource.getEncryptPsw();
        if(uResource.getEncryption() != null)this.encryption = uResource.getEncryption();
        if(uResource.getDeleteFlag() != null)this.deleteFlag = uResource.getDeleteFlag();
        if(uResource.getShareFlag() != null)this.shareFlag = uResource.getShareFlag();
        if(uResource.getParentId() != null)this.parentId = uResource.getParentId();
        if(uResource.getTypeId() != null)this.typeId = uResource.getTypeId();
    }
}
