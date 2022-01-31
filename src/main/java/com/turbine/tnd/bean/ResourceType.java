package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/25 14:26
 */

@Data
@ToString
public class ResourceType {
    private int id;
    private String type;
    //状态 1为禁用 0 为启用
    private boolean state;
}
