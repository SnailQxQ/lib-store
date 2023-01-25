package com.turbine.tnd.dto;

import lombok.Data;
import lombok.ToString;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author Turbine
 * @Description
 * @date 2023/1/6 21:28
 */
@Data
@ToString
public class ShareResourceDTO {
    //仅为文件资源时使用 用户资源id
    private Integer userResourceId;
    //资源id 文件或文件夹
    //private Integer resourceId;
    //资源名 or 文件夹名
    private String originalName;
    //提取码
    private String fetchCode;

    private Timestamp createTime;
    //提取码有效时间，单位分钟
    private Integer survivalTime;
    //资源类型 0:文件夹 1:文件
    private Integer type;


    public boolean isValid(){
        boolean re = false;
        if(type != null){
            if(type == 1 ){
                if(this.fetchCode != null && this.survivalTime != null )re =true;
            }else if(type == 0){
                if(this.userResourceId != null && this.fetchCode != null && this.survivalTime != null)re = true;
            }
        }

        return re;
    }
}
