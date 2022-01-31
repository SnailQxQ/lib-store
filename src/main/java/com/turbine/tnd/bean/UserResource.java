package com.turbine.tnd.bean;

import lombok.Data;

import java.sql.Date;

/**
 * @author Turbine
 * @Description 用户资源Bean
 * @date 2022/1/29 15:35
 */
@Data
public class UserResource {
    //资源id
    private int id;
    //上传用户id
    private int u_id;
    //文件名 MD5资源标识
    private String fileName;
    //上传时间
    private Date uploadTime;
    //原文件名
    private String originalName;
    //加密密码
    private String encryptPsw;
    //是否加密
    private boolean encryption=false;
    //删除标记
    private boolean d_flag=false;
    //是否公开资源
    private boolean s_flag=false;

}
