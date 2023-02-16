package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine 共享资源表bean
 * @Description:
 * @date 2023/2/6 14:26
 */
@Data
@ToString
public class PublicResource {
    private Integer id;
    //共享名
    private String name;
    //资源简介
    private String intro;
    //实际资源存储的id
    private Integer userResourceId;
    //共享资源类型，文件or 文件夹
    private Integer type;
    //浏览次数
    private Integer views;
    //下载次数
    private Integer collectNum;
    private Integer userId;

    public PublicResource(){}

    public PublicResource(String name, String intro, Integer userResourceId,Integer type, Integer views, Integer collectNum,Integer userId){
        this.userResourceId = userResourceId;
        this.name = name;
        this.intro = intro;
        this.type = type;
        this.views = views;
        this.collectNum = collectNum;
        this.userId = userId;
    }


}
