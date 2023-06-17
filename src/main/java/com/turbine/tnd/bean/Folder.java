package com.turbine.tnd.bean;

import lombok.Data;

import java.sql.Date;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/14 23:18
 */
@Data
public class Folder {

    private Integer folderId;
    private Date createTime;
    private String folderName;
    private Integer userId;
    private Integer parentId;
    //是否删除
    private Boolean deleteFlag;
    //是否共享
    private  Boolean  shareFlag;
    //是否为收藏资源
    private Boolean collect = false;

    public  Folder(){}

    public Folder(int folderId, Date createTime, String folderName, int userId, int parentId) {
        this.folderId = folderId;
        this.createTime = createTime;
        this.folderName = folderName;
        this.userId = userId;
        this.parentId = parentId;
    }

    public Folder(Date createTime, String folderName, int userId, int parentId) {
        this.createTime = createTime;
        this.folderName = folderName;
        this.userId = userId;
        this.parentId = parentId;
    }

    public Folder(int userId, String folderName, int parentId) {
        this.userId = userId;
        this.folderName = folderName;
        this.parentId = parentId;
    }
}
