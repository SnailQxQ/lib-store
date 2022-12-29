package com.turbine.tnd.service;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.*;
import com.turbine.tnd.utils.Filter;
import com.turbine.tnd.utils.FilterFactor;
import com.turbine.tnd.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/19 16:28
 */
@Service
@Slf4j
public class UserService {
    @Autowired
    UserDao udao;
    @Autowired
    ResourceDao rdao;
    @Autowired
    FilterFactor filterFactor;
    @Autowired
    SimpleFileService fs;
    @Autowired
    FolderDao fdao;
    @Autowired
    UserResourceDao urdao;
    @Autowired
    @Qualifier("EruptUploadStrategy")
    SliceFileService supload;

    @Autowired
    ResourceRecycleDao rrdao;

    /**
     * 通过记住我的方式登陆就验证登陆序列，否则就验证密码
     * 如果是通过密码登入 则也要更新登入序列sequence
     *
     * @param user     用户bean
     * @param remember 是否记住我
     * @param request
     * @return
     */
    public Message verify(User user, boolean remember, HttpServletRequest request) {
        boolean re = false;
        Message me = new Message();
        User result = null;
        UserDTO udto = new UserDTO();

        if (remember) {

            if ((result = udao.inquireBySequence(user)) != null) re = true;

        } else {
            //加密密码
            String s = MD5Util.saltEnryption(user.getPassword());
            user.setPassword(s);
            if ((result = udao.inquireByPsw(user)) != null) re = true;
            if (re) {
                //使用密码登陆成功后就更新登陆序列
                //得到更新后的新序列号
                String sequence = updateSequence(result.getId(), result.getUserName());
                if ("".equals(sequence)) re = false;
                else result.setSequence(sequence);
            }
        }

        if (re) {
            me.setCode(200);
            me.setMessage("success");
            //MD5Util.enryptionByKey(result.getUserName(),result.getPassword());
            String token = MD5Util.randomSaltEnryption(result.getUserName());

            udto.setToken(token);
            if (!remember) udto.setSequence(result.getSequence());
            udto.setUserName(result.getUserName());
            //登入成功 更新token
            request.getSession().setAttribute(udto.getUserName(), udto.getToken());

            me.setData(udto);

        } else {
            me.setCode(400);
            me.setMessage("账号或密码输入错误！");
        }

        return me;
    }

    /**
     * 更新用户的登入序列号
     *
     * @param id 用户id
     * @return 更新后的登入序列，若更新失败则返回空串
     */
    private String updateSequence(int id, String userName) {
        User user = new User();

        String sequence = MD5Util.randomSaltEnryption(userName);
        user.setId(id);
        user.setSequence(sequence);

        int re = udao.updateUser(user);
        return re == 1 ? sequence : "";
    }


    /**
     * @param multipartFile
     * @param userName
     * @return
     */
    public boolean simpleUpload(MultipartFile multipartFile, String userName, Message message, int parentId) {

        String originalName = multipartFile.getOriginalFilename();
        Filter<String> filter = filterFactor.getResource(FilterFactor.filterOpt.AC_FILTER);
        int idx = originalName.lastIndexOf(".");
        String suffix = originalName.substring(idx);
        String fileName = filter.filtration(originalName.substring(0, idx));

        System.out.println("文件名：" + fileName);
        System.out.println("文件后缀：" + suffix);

        //获取用户id 和类型id
        User user = udao.inquireByName(userName);
        ResourceType type = fs.inquireType(suffix);
        if (type == null) {
            message.setResultCode(ResultCode.ERROR_400);
            message.setMessage("文件类型不支持！");
            return false;
        }

        return fs.upload(multipartFile, fileName, user.getId(), type.getId(), suffix, parentId);
    }


    /**
     * 分片上传dto
     *
     * @param fudto
     * @return
     */
    public Message sliceUpload(FileRequestDTO fudto) {
        //
        Message message = new Message();
        Resource resource = null;
        User user = udao.inquireByName(fudto.getUserName());

        if (!supload.isSupport(fudto.getOriginalName())) {
            message.setResultCode(ResultCode.ERROR_400);
            message.setMessage("文件类型不支持！");
            FileUploadDTO fdto = new FileUploadDTO();
            fdto.setAccomplish(false);
            fdto.setAllSuccess(false);
            log.debug(fudto.getOriginalName() + "文件类型不支持！");
            message.setData(fdto);
        }else if ((resource = rdao.inquireByName(fudto.getFileName())) != null) {
            message.setResultCode(ResultCode.SUCCESS);
            FileUploadDTO fdto = new FileUploadDTO();
            fdto.setChunkNum(fudto.getChunkNum());
            fdto.setAccomplish(true);
            fdto.setAllSuccess(true);
            supload.deleteTempFile(fudto);
            //不同文件夹下也可以创建相同的文件
            UserResource ur = urdao.inquireUserResourceByName(user.getId(), fudto.getFileName(),fudto.getOriginalName(),fudto.getParentId());
            if(ur == null){
                ur = new UserResource(user.getId(), fudto.getFileName(), fudto.getOriginalName(), fudto.getParentId(), resource.getType_id());
                ur.setUploadTime(new Date(System.currentTimeMillis()));
                urdao.addUserResource(ur);
            }

            ResourceDTO dto = new ResourceDTO(ur);
            dto.setFileType( fudto.getOriginalName().substring(fudto.getOriginalName().lastIndexOf(".")) );
            dto.setFileSize(fudto.getTotalSize());

            fdto.setResource(dto);
            log.debug("文件已经在服务器有了！");
            message.setData(fdto);
        }
        else {

            message.setResultCode(ResultCode.SUCCESS);
            message.setData(supload.sliceUpload(fudto));
        }


        return message;
    }

    /**
     * 分片下载
     *
     * @param fdto
     * @return
     */
    public Message sliceDownload(FileRequestDTO fdto) {
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(supload.sliceDownload(fdto));

        return message;
    }

    /**
     * 查询自己的文件
     *
     * @param parentId 父文件id
     * @param userName
     * @return
     */
    public Message inquireOwnFile(int parentId, String userName) {
        Message message = new Message();
        User user = udao.inquireByName(userName);
        List<FolderDTO> folders = udao.inquireUserFolders(parentId, user.getId(), false);
        List<ResourceDTO> resources = rdao.inquireUserResourceByParentId(parentId, user.getId(), false);
        ResourceFolder rfdto = new ResourceFolder();
        rfdto.setFolders(folders);
        rfdto.setResources(resources);
        message.setResultCode(ResultCode.SUCCESS);
        message.setData(rfdto);

        return message;
    }

    /**
     * 根据资源md5 id 判断文件是否存在
     * @param userName  用户名
     * @param fileName  资源文件md5标识
     * @param parentId  父文件id
     * @return
     */
    public boolean ResourceIsExist(String userName, String fileName, Integer parentId) {
        boolean flag = false;
        User user = udao.inquireByName(userName);
        if (user != null) {
            if (rdao.hasResource(user.getId(), fileName,parentId) > 0) flag = true;
        }

        return flag;
    }

    /**
     * 根据资源id 判断用户是否持有该资源
     * @param resourceId    用户id
     * @param userName      用户名
     * @return
     */
    public boolean ResourceIsExist(int resourceId, String userName) {
        boolean flag = false;

        UserResource userResource = urdao.inquireUserResourceById(resourceId);
        User user = udao.inquireByName(userName);
        if(userResource != null && userResource.getU_id() == user.getId())flag = true;

        return flag;
    }

    //创建文件夹
    public Message mkdirFolder(int parentId, String userName, String folderName) {
        Message message = new Message();
        User user = udao.inquireByName(userName);
        Folder folder = new Folder(new Date(System.currentTimeMillis()), folderName, user.getId(), parentId);

        if (user != null && udao.addUserFolder(folder) > 0) {
            FolderDTO folderDTO = new FolderDTO();
            folderDTO.setFolderId(folder.getFolderId());
            folderDTO.setFolderName(folderName);
            folderDTO.setCreateTime(folder.getCreateTime());

            message.setResultCode(ResultCode.SUCCESS);
            message.setData(folderDTO);
        } else {
            message.setResultCode(ResultCode.ERROR_500);
        }
        return message;
    }


    public Resource inquireResource(String fileName) {
        return rdao.inquireByName(fileName);
    }

    //查询分片上传的进度

    /**
     * @param param 文件hashId
     * @return
     */
    public Message inquireSliceProgress(FileRequestDTO param) {
        Message message = new Message(ResultCode.SUCCESS);

        if (!fs.hasExist(param.getFileName())) {
            List<Integer> list = supload.checkFinished(param);
            message.setData(list);
        } else {
            FileUploadDTO fdto = new FileUploadDTO();
            fdto.setAccomplish(true);
            fdto.setAllSuccess(true);
        }

        return message;
    }


    /**
     * @param fileId
     * @param newName
     * @return
     */
    public Message modifyFileName(String fileId, String newName) {
        Message message = new Message();
        if (udao.modifyFileName(fileId, newName) > 0) message.setResultCode(ResultCode.SUCCESS);
        else message.setResultCode(ResultCode.ERROR_500);

        return message;
    }

    //判断用户是否有该文件夹
    public boolean FolderIsExist(int folderId) {
        return fdao.inquireFolder(folderId) != null ;
    }

    //修改文件夹信息
    public Message modifyFolder(Folder folder, String userName) {
        Message message = new Message(ResultCode.ERROR_500);
        User user = udao.inquireByName(userName);
        if (user != null) {
            folder.setUserId(user.getId());
            if (fdao.modifyFolder(folder) > 0) message.setResultCode(ResultCode.SUCCESS);
        }

        return message;
    }

    //TODO:同名文件怎么处理
    //修改用户资源
    public Message modifyUserResource(UserResource uResource, String userName) {
        Message message = new Message(ResultCode.ERROR_500);
        User user = udao.inquireByName(userName);
        if (user != null) {
            uResource.setU_id(user.getId());
            UserResource ur = urdao.inquireUserResourceById(uResource.getId());
            if(ur != null){
                System.out.println(ur);
                ur.assemble(uResource);
                //要是为文件：移动的目的地要是已经存在该文件则直接删除当前层里文件记录
                if (urdao.modifyResource(ur) > 0) message.setResultCode(ResultCode.SUCCESS);

                message.setResultCode(ResultCode.SUCCESS);
            }else message.setResultCode(ResultCode.ERROR_404);
        }
        return message;
    }

    /**
     * @param fileId
     * @return
     */
    public Message delUserResource(Integer fileId, int model, String userName,Integer parentId) {
        Message message = new Message();
        User user = udao.inquireByName(userName);
        message.setResultCode(ResultCode.ERROR_500);

        if (user != null) {

                UserResource ur =  urdao.inquireUserResourceById(fileId);
                ResourceRecycle rr = new ResourceRecycle();
                rr.setDeleteTime(new Date(System.currentTimeMillis()));
                rr.setOriginalName(ur.getOriginalName());
                rr.setResourceId(ur.getId());
                rr.setParentId(ur.getParentId());
                rr.setU_id(user.getId());
                rr.setTypeId(1);

                if (model == 0 && ur != null &&  !ur.getD_flag()) {
                    ur.setD_flag(true);
                    if (urdao.modifyResource(ur) > 0 && rrdao.addResourceRecycle(rr) > 0) {
                        message.setResultCode(ResultCode.SUCCESS);
                    }
                }else if(model == 1){

                    if (rrdao.removeResourceRecycle(rr) > 0 && urdao.removeResource(ur) > 0) message.setResultCode(ResultCode.SUCCESS);
                }

        }

            return message;
        }

    /**
     *
     * @param folderId  文件夹id
     * @param userName  文件夹名
     * @param del       是否物理删除
     * @return
     */
        public Message delUserFolder ( int folderId, String userName,boolean del){
            Message message = new Message(ResultCode.ERROR_500);
            ResourceRecycle rr = new ResourceRecycle();
            User user = udao.inquireByName(userName);
            if(user != null){
                Folder folder = fdao.inquireFolder(folderId);

                rr.setResourceId(folder.getFolderId());
                rr.setOriginalName(folder.getFolderName());
                rr.setParentId(folder.getParentId());
                rr.setU_id(user.getId());
                rr.setDeleteTime(new Date(System.currentTimeMillis()));
                rr.setTypeId(0);

                if (setUserFolderStatus(folderId,user.getId(),del,true) ){
                    if(!del) rrdao.addResourceRecycle(rr);
                    else rrdao.removeResourceRecycle(rr);
                    message.setResultCode(ResultCode.SUCCESS);
                }
            }

            return message;
        }


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
            folder.setD_flag(delFlag);

            boolean flag = false;

            //逻辑删除
            if (!del ) {
                fdao.modifyFolder(folder);//设置文件夹自己为删除状态
                List<FolderDTO> folders = udao.inquireUserFolders(folderId, userId, !delFlag);
                List<ResourceDTO> resources = rdao.inquireUserResourceByParentId(folderId, userId, !delFlag);
                if (resources != null) {
                    for (ResourceDTO re : resources) {
                        UserResource newRe = new UserResource();
                        newRe.setId(re.getId());
                        newRe.setFileName(re.getFileId());
                        newRe.setD_flag(delFlag);
                        newRe.setU_id(userId);

                        urdao.modifyResource(newRe);
                    }
                }
                if (folders != null) {
                    for (FolderDTO ele : folders) {
                        setUserFolderStatus(ele.getFolderId(),userId,del,delFlag);
                    }
                }

                flag = true;
            }else if(del){
                //物理删除
                folder.setUserId(userId);
                fdao.removeFolder(folder);
                List<FolderDTO> folders = udao.inquireUserFolders(folderId, userId, !delFlag);
                List<ResourceDTO> resources = rdao.inquireUserResourceByParentId(folderId, userId, !delFlag);
                if (resources != null) {
                    for (ResourceDTO re : resources) {
                        UserResource newRe = new UserResource();
                        newRe.setFileName(re.getFileId());
                        newRe.setU_id(userId);

                        urdao.removeResource(newRe);
                    }
                }
                if (folders != null) {
                    for (FolderDTO ele : folders) {
                        setUserFolderStatus(ele.getFolderId(),userId,del,delFlag);
                    }
                }
                flag = true;
            }

            return flag;
        }



        //查询用户已回收的资源
        public Message inquireRecycleResource (String userName){
            User u = udao.inquireByName(userName);
            Message message = new Message(ResultCode.SUCCESS);

            if (u != null) {
                List<ResourceRecycle> rrs =  rrdao.inquireAll(u.getId());
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
                ur.setD_flag(false);
                flag = recoverResource(ur);
            }

            return flag;
        } */
        public boolean recoverResource(Integer fileId) {
            boolean flag = false;
            UserResource ur = urdao.inquireUserResourceById(fileId);
            ur.setD_flag(false);
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

            Folder parentFolder = fdao.inquireFolder(folderId);
            boolean flag = false;

            if (parentFolder != null){
                parentFolder.setD_flag(false);
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

            return rrdao.removeResourceRecycle(rr) > 0 && setUserFolderStatus(folder.getFolderId(), folder.getUserId(),false,false) ;
        }


        public boolean recoverFolder(Integer folderId, String userName) {
            boolean flag = false;
            User user = udao.inquireByName(userName);
            if(user != null){
                Folder folder = fdao.inquireFolder(folderId);
                flag = recoverFolder(folder,user.getId());
            }

            return flag;
        }



}