package com.turbine.tnd.dto;

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
}
