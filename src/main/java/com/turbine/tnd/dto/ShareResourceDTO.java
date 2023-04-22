package com.turbine.tnd.dto;

import com.turbine.tnd.bean.ShareResource;
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
    //仅为文件资源时使用 用户资源id (文件或文件夹)
    private Integer userResourceId;
    //资源id
    //private Integer resourceId;
    private String shareName;
    //资源名 or 文件夹名
    private String originalName;
    //提取码
    private String fetchCode;

    private Timestamp createTime;
    //提取码有效时间，单位分钟
    private Integer survivalTime;
    //分享链接点击次数
    private Integer clicks;
    //分享资源下载次数
    private Integer downloads;
    //资源类型 0:文件夹 1:文件
    private Integer type;
    //是否过期
    private Boolean valid;
    //分享者id
    private Integer shareUId;

    public ShareResourceDTO(){}

    public ShareResourceDTO(ShareResource sr) {
        assemble(sr);
    }


    //注意isValid 会被@Data 当成valid属性的get 方法
    public boolean verify(){
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

    //资源名要查数据库 放在 service层进行装配
    public void assemble(ShareResource sr){
        this.userResourceId = sr.getUserResourceId();
        this.shareName = sr.getShareName();
        this.fetchCode = sr.getFetchCode();
        this.createTime = sr.getCreateTime();
        this.survivalTime = sr.getSurvivalTime();
        this.clicks = sr.getClicks();
        this.downloads = sr.getDownloads();
        this.type = sr.getType();
        this.valid = (sr.getCreateTime().getTime()+(long)this.survivalTime*60*1000 - System.currentTimeMillis()) > 0;
        this.shareUId = sr.getUserId();
    }
}
