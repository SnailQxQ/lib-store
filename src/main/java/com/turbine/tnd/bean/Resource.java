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
    int id;
    String name;
    /*unit Mb*/
    int size;
    int type_id;
    String location;
    /*unique md5 identification for identify file*/
    String identification;
    boolean encryption;
}
