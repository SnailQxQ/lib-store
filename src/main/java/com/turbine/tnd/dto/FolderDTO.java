package com.turbine.tnd.dto;

import com.turbine.tnd.bean.Folder;
import lombok.Data;

import java.sql.Date;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/8 12:13
 */
@Data
public class FolderDTO {

    private int folderId;
    private Date createTime;
    private String folderName;
    private Boolean collect;
    private Boolean share;

    FolderDTO(){}

    public FolderDTO(Folder folder){
        this.folderId = folder.getFolderId();
        this.createTime = folder.getCreateTime();
        this.folderName = folder.getFolderName();
        this.collect = folder.getCollect();
        this.share = folder.getShareFlag();
    }
}
