package com.turbine.tnd.service;

import com.turbine.tnd.bean.Folder;
import com.turbine.tnd.bean.UserResource;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.FolderDTO;
import com.turbine.tnd.dto.ResourceDTO;
import com.turbine.tnd.utils.FilterFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/16 16:45
 */
@Service
@Slf4j
@SuppressWarnings("all")
public class ResourceService {

    @Autowired
    UserDao udao;
    @Autowired
    ResourceDao rdao;
    @Autowired
    FilterFactor filterFactor;
    @Autowired
    FolderDao fdao;
    @Autowired
    UserResourceDao urdao;



    /**
     *
     * @param folderId  要进行操作的文件id
     * @param userId    操作者
     * @param del       删除标识，是逻辑修改还是物理删除
     * @param delFlag    删除标记，仅在逻辑修改状态生效
     * @return
     */
    //递归更新删除标记
    @Transactional
    public boolean setUserFolderStatus ( int folderId, Integer userId,boolean del,boolean delFlag){
        Folder folder = new Folder();
        folder.setFolderId(folderId);
        folder.setDeleteFlag(delFlag);

        boolean flag = false;

        //逻辑删除
        if (!del ) {
            fdao.modifyFolder(folder);//设置文件夹自己为删除状态
            List<Folder> folders = fdao.inquireUserFolders(folderId, userId, !delFlag,null);
            List<ResourceDTO> resources = rdao.inquireUserResourceByParentId(folderId, userId, !delFlag,null);
            if (resources != null) {
                for (ResourceDTO re : resources) {
                    UserResource newRe = new UserResource();
                    newRe.setId(re.getId());
                    newRe.setFileName(re.getFileId());
                    newRe.setDeleteFlag(delFlag);
                    newRe.setU_id(userId);

                    urdao.modifyResource(newRe);
                }
            }
            if (folders != null) {
                for (Folder ele : folders) {
                    setUserFolderStatus(ele.getFolderId(),userId,del,delFlag);
                }
            }

            flag = true;
        }else if(del){
            //物理删除
            folder.setUserId(userId);
            fdao.removeFolder(folder);
            List<Folder> folders = fdao.inquireUserFolders(folderId, userId, !delFlag,null);
            List<ResourceDTO> resources = rdao.inquireUserResourceByParentId(folderId, userId, !delFlag,null);
            if (resources != null) {
                for (ResourceDTO re : resources) {
                    UserResource newRe = new UserResource();
                    newRe.setResourceId(re.getId());
                    //newRe.setFileName(re.getFileId());
                    newRe.setU_id(userId);

                    urdao.removeResource(newRe);
                }
            }
            if (folders != null) {
                for (Folder ele : folders) {
                    setUserFolderStatus(ele.getFolderId(),userId,del,delFlag);
                }
            }
            flag = true;
        }

        return flag;
    }


    /**
     * @Description: 递归修改文件夹以及文件内资源 删除标记 和 分享标记
     * @author Turbine
     * @param
     * @param folder
     * @date 2023/4/22 18:14
     */
    public void recurModifyFolder(Folder folder){

        fdao.modifyFolder(folder);//设置文件夹自己为删除状态
        List<Folder> folders = fdao.inquireFolders(folder.getFolderId(), folder.getUserId(), null, null);

        List<ResourceDTO> resources = rdao.inquireUserResourceByParentId(folder.getFolderId(), folder.getUserId(), null,null);
        if (resources != null) {
            for (ResourceDTO re : resources) {
                UserResource newRe = new UserResource();
                newRe.setId(re.getId());
                newRe.setFileName(re.getFileId());
                newRe.setDeleteFlag(folder.getDeleteFlag() );
                newRe.setShareFlag(folder.getShareFlag() );
                newRe.setU_id(folder.getUserId());

                urdao.modifyResource(newRe);
            }
        }
        if (folders != null) {
            for (Folder f : folders) {
                f.setShareFlag(folder.getShareFlag());
                f.setDeleteFlag(folder.getDeleteFlag());
                recurModifyFolder(f);
            }
        }

    }

}
