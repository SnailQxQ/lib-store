package com.turbine.tnd.dto;

import com.turbine.tnd.bean.PublicResource;
import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/6 18:30
 */
@Data
@ToString
public class PublicResourceDTO {
    //
    private Integer id;
    //共享名
    private String name;
    //资源简介
    private String intro;
    //实际资源存储的id 文件ID or 文件夹ID
    private Integer userResourceId;
    //共享资源类型，文件or 文件夹
    private Integer type;
    //浏览次数
    private Integer views;
    //下载次数
    private Integer collectNum;
    private Integer userId;
    //仅做接收使用
    private  Integer parentId;
    //资源是否已经分享者取消
    public  Boolean d_flag;


    public void assemble(PublicResource e) {
        this.id = e.getId();
        this.name = e.getName();
        this.intro = e.getIntro();
        this.userResourceId = e.getUserResourceId();
        this.type = e.getType();
        this.views = e.getViews();
        this.collectNum = e.getCollectNum();
        this.userId = e.getUserId();
    }
}
