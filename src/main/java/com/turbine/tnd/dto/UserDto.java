package com.turbine.tnd.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/19 19:46
 */
@Data
@ToString
public class UserDto {
    private String token;
    private String userName;
    private String sequence;
}
