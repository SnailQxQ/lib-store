package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/6 21:53
 */
@Data
@ToString
public class SubResource {
    private Integer id;
    private Integer userId;
    private Integer publicResourceId;
    private Boolean isdeleteFlag = false;//不能用 deleteFlag ibatis解会出问题 注入不了值。

    public SubResource(){}

    public SubResource(Integer userId,Integer publicResourceId){
        this.userId = userId;
        this.publicResourceId = publicResourceId;
    }
}
