package com.turbine.tnd.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/8 12:14
 */

@Data
public class ResourceFolder {
    List<FolderDTO> folders;
    List<ResourceDTO> resources;

}
