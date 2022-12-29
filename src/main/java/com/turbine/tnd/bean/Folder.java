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
    private int folderId;
    private Date createTime;
    private String folderName;
    private int userId;
    private int parentId;
    //是否删除
    private boolean d_flag;
    //是否共享
    private  boolean s_flag;


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
