package com.turbine.tnd.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/26 23:21
 */

@ToString
@Data
public class UserDTO {
    private String token;
    private String userName;
    private String sequence;
}
