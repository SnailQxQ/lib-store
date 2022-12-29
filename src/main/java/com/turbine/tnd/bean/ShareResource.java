package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

import java.sql.Date;

/**
 * @author Turbine
 * @Description
 * @date 2022/3/28 15:15
 */
@Data
@ToString
public class ShareResource {
    Integer id;
    String shareCode;
    Integer u_id;
    Boolean encrypt;
    Date createTime;
    Integer survivalTime;
    Integer clicks;
    Integer downloads;

}
