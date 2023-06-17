package com.turbine.tnd.dto;

import com.turbine.tnd.bean.Folder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/8 12:14
 */

@Data
public class ResourceFolder {
    private List<FolderDTO> folders;
    private List<ResourceDTO> resources;

    //本来想重载， 这里两个list 由于泛型擦除会变成两个objct。因此不能满足重载 参数类型不同的条件，所以会报错
    public void setFolders(List<FolderDTO> folders) {
        this.folders = folders;
    }

    public void assembleFolderDTO(List<Folder> folders) {
        this.folders = new ArrayList<>();
        for (Folder f: folders) {
            FolderDTO fdto = new FolderDTO(f);
            this.folders.add(fdto);
        }
    }
}
