package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

import java.sql.Date;

/**
 * @author Turbine
 * @Description
 * @date 2022/3/28 15:15
 */
@Data
@ToString
public class ShareResource {
    Integer id;
    //原资源名
    String originalName;
    //分享文件标识
    String shareName;
    //6位提取码
    String fetchCode;
    //用户id
    Integer userId;
    //用户资源表id
    Integer userResourceId;

    Date createTime;
    //分享存在时间
    Integer survivalTime;
    //点击次数
    Integer clicks;
    //下载次数
    Integer downloads;

}
