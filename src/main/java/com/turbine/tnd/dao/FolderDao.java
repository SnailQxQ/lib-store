package com.turbine.tnd.dao;

import com.turbine.tnd.bean.Folder;
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
}
