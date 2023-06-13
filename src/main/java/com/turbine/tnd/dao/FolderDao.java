package com.turbine.tnd.dao;

import com.turbine.tnd.bean.Folder;
import com.turbine.tnd.dto.FolderDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/14 23:27
 */
@Mapper
public interface FolderDao {
    int modifyFolder(Folder folder);

    Folder inquireFolderById(int folderId);

    Folder inquireFolder(int folderId,int userId);

    int removeFolder(Folder folder);

    int addFolder(Folder folder);

    Folder inquireParent(Integer id);

    List<Folder> inquireCollectFolder(Integer userId, Boolean isRecycle, Boolean collect);

    List<Folder> inquireFolders(Integer parentId, Integer userId, Boolean isRecycle, Boolean isCollect);

    //查询指定用户下的指定文件夹下的所有文件夹
    List<FolderDTO> inquireUserFolders(Integer parentId, int userId, Boolean isRecycle, Boolean isCollect);

    FolderDTO inquireUserFolderById(int folderId);
    //创建文件目录
    int addUserFolder(Folder folder);
}
