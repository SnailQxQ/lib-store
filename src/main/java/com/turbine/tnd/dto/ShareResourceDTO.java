package com.turbine.tnd.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine
 * @Description
 * @date 2023/1/6 21:28
 */
@Data
@ToString
public class ShareResourceDTO {
    private Integer userResourceId;

    private Integer resourceId;
    //资源名
    private String originalName;
    //提取码
    private String fetchCode;
    //提取码有效时间，单位分钟
    private Integer survivalTime;
}
