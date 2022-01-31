package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

/**
 * @author 邱信强
 * @Description Resource 资源类
 * @date 2022/1/19 15:05
 */
@Data
@ToString
public class Resource {

    private int id;
    /*unit Mb*/
    private long size;
    private int type_id;
    private String location;
    //文件MD5 唯一标识名
    private String fileName;
    private ResourceType type;
}
