package com.turbine.tnd.service;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.utils.FilterFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/16 16:42
 */

@Service
@Slf4j
@SuppressWarnings("all")
public class RecycleResourceService {
    @Autowired
    UserDao udao;
    @Autowired
    ResourceDao rdao;
    @Autowired
    FolderDao fdao;
    @Autowired
    UserResourceDao urdao;
    @Autowired
    UserResourceService s_ur;
    @Autowired
    ResourceRecycleDao rrdao;


    //查询用户已回收的资源
    public Message inquireRecycleResource (String userName){
        User u = udao.inquireByName(userName);
        Message message = new Message(ResultCode.SUCCESS);

        if (u != null) {
            List<ResourceRecycle> rrs =  rrdao.inquireAll(u.getId());
            for (ResourceRecycle e : rrs){
               if(e.getTypeId() == 1) e.setThumbnail(s_ur.getThumbnail(e.getResourceId()));
            }
            message.setData(rrs);
        }
        return message;
    }

    private boolean recoverResource(UserResource ur){
        ResourceRecycle rr = new ResourceRecycle();
        rr.setU_id(ur.getU_id());
        rr.setResourceId(ur.getId());
        boolean flag = rrdao.removeResourceRecycle(rr)>0 && urdao.modifyResource(ur) > 0;
        //恢复父文件
        if(ur.getParentId() != 0 ){
            flag = recoverFolder(ur.getParentId(),ur.getU_id());
        }

        return flag;
    }

    //恢复资源 如果父文件存在且删除标记为true 则递归重置标记，否则就新建一个父文件夹
       /* public boolean recoverResource(String fileId, String userName,Integer parentId) {
            boolean flag = false;
            User user = udao.inquireByName(userName);
            if(user != null){
                UserResource ur = urdao.inquireUserResourceByName(user.getId(), fileId,parentId);
                ur.setdeleteFlag(false);
                flag = recoverResource(ur);
            }

            return flag;
        } */

    public boolean recoverResource(Integer userResourceId) {
        boolean flag = false;
        UserResource ur = urdao.inquireUserResourceById(userResourceId);
        ur.setDeleteFlag(false);
        flag = recoverResource(ur);

        return flag;
    }

    //文件存在就恢复否则就新建

    /**
     * 递归恢复文件夹
     * 恢复文件的时候需要恢复父文件使用
     * @param folderId  文件夹id
     * @param userId    创建者id
     * @return
     */
    private boolean recoverFolder(Integer folderId,Integer userId) {
        //0为根文件
        if(folderId == 0)return true;

        Folder parentFolder = fdao.inquireFolderById(folderId);
        boolean flag = false;

        if (parentFolder != null){
            parentFolder.setDeleteFlag(false);
            fdao.modifyFolder(parentFolder);
            flag = recoverFolder(parentFolder.getParentId(),userId);
        }else{
            Folder folder = new Folder();
            folder.setFolderName("新建文件夹");
            folder.setFolderId(folderId);
            folder.setUserId(userId);
            folder.setCreateTime(new Date(System.currentTimeMillis()));
            folder.setParentId(0);
            flag = udao.addUserFolder(folder) > 0;
        }
        return  flag;
    }


    //恢复文件夹 单独恢复文件夹用
    private boolean recoverFolder(Folder folder,Integer userId) {
        ResourceRecycle rr = new ResourceRecycle();
        rr.setResourceId(folder.getFolderId());
        rr.setU_id(userId);

        return rrdao.removeResourceRecycle(rr) > 0 &&  s_ur.setUserFolderStatus(folder.getFolderId(), folder.getUserId(),false,false) ;
    }


    public boolean recoverFolder(Integer folderId, String userName) {
        boolean flag = false;
        User user = udao.inquireByName(userName);
        if(user != null){
            Folder folder = fdao.inquireFolderById(folderId);
            flag = recoverFolder(folder,user.getId());
        }

        return flag;
    }


}
