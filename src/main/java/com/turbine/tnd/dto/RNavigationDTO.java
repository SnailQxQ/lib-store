package com.turbine.tnd.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine
 * @Description: 资源导航dto
 * @date 2023/2/2 21:37
 */

@Data
@ToString
public class RNavigationDTO {
    private String name;
    private Integer id;
    RNavigationDTO parent;
}
