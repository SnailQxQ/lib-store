package com.turbine.tnd.service;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.*;
import com.turbine.tnd.utils.Filter;
import com.turbine.tnd.utils.FilterFactor;
import com.turbine.tnd.utils.MD5Util;
import com.turbine.tnd.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    UserShareResourceDao usrdao;

    @Autowired
    @Qualifier("EruptUploadStrategy")
    SliceFileService supload;

    @Autowired
    ResourceRecycleDao rrdao;

    @Value("${file.upload.tmpDir}")
    String tempDir;
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
        Message message = new Message(ResultCode.ERROR_500);
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

            int idx = fudto.getOriginalName().lastIndexOf(".");
            String originalName = fudto.getOriginalName().substring(0,idx);
            UserResource ur = new UserResource(user.getId(),resource.getId(),fudto.getFileName(), originalName, fudto.getParentId(), resource.getType_id());
            ur.setUploadTime(new Date(System.currentTimeMillis()));
            urdao.addUserResource(ur);

            ResourceDTO dto = new ResourceDTO(ur);
            dto.setFileType( fudto.getOriginalName().substring(idx));
            dto.setFileSize(fudto.getTotalSize());

            fdto.setResource(dto);
            log.debug("文件已经在服务器有了！");
            message.setData(fdto);
        }else {
            //未上传进行上传
            FileUploadDTO fdto = supload.sliceUpload(fudto);
            if(fdto != null){
                message.setData(fdto);
                message.setResultCode(ResultCode.SUCCESS);
            }
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
     * @param resourceId    资源id
     * @param userName      用户名
     * @return
     */
    public boolean hasResource(int resourceId, String userName) {
        boolean flag = false;

        User user = udao.inquireByName(userName);
        if(user != null){
            if(urdao.countUserResource(resourceId,user.getId()) > 0 )flag = true;
        }


        return flag;
    }

    /**
     * @Description: 判断用户是否持有该资源
     * @author Turbine
     * @param
     * @param userResourceId
     * @param userId
     * @return boolean
     * @date 2023/1/13 12:22
     */
    public boolean hasResource(int userResourceId, Integer userId) {
        return urdao.inquireUserResourceById(userResourceId).getU_id() == userId ;
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


    public Resource inquireResource(Integer resourceId) {
        UserResource userResource = urdao.inquireUserResourceById(resourceId);
        return rdao.inquireByName(userResource.getFileName());
    }


    //查询共享资源 //TODO :区分文件和文件夹 文件夹需要进行压缩传输
    public void getShareResource(String shareName, String userName, HttpServletResponse resp) throws IOException {
        ShareResource sr = usrdao.inquireShareResourceBysName(shareName);
        OutputStream os = resp.getOutputStream();

        if(sr != null && !resourceIsExpire(sr.getCreateTime(),sr.getSurvivalTime()) ){
            if(sr.getType() == 1){
                UserResource ur = urdao.inquireUserResourceById(sr.getUserResourceId());
                if(ur != null){
                    Resource resource = rdao.inquireByName(ur.getFileName());
                    File file = new File(resource.getLocation());
                    int read = 0;
                    byte[] data = new byte[1024];
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

                    while((read = bis.read(data)) != -1){
                        os.write(data,0,read);
                    }

                    resp.addHeader("Content-Disposition","attchement;filename=" +resource.getFileName()+resource.getType().getType());
                }
            }else if(sr.getType() == 0){
                resp.addHeader("Content-Disposition","attchement;filename=" +MD5Util.randomSaltEnryption(shareName)+".zip");
                User user = udao.inquireByName(userName);
                Integer userId = user.getId();
                getShareFolder(sr,userId,os);
            }
        }

    }

    //下载的资源为文件夹形式需要进行压缩操作再发送（保持原有的文件结构）
    private void getShareFolder(ShareResource sr, Integer userId, OutputStream os) {
        int folderId = sr.getUserResourceId();
        List<FolderDTO> folderDTOS = udao.inquireUserFolders(folderId, userId, false);

        UserResource ur = urdao.inquireUserResourceById(sr.getUserResourceId());
        File zipF = new File(tempDir + File.separator + ur.getOriginalName() + ".zip");
        try(ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipF)))) {
            zos.setMethod(ZipOutputStream.DEFLATED);//zip格式

            buildCompressFolder(zos,folderId,userId,"");
            byte[] data = new byte[1024];
            int read = 0;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipF));
            while( (read = bis.read(data) ) != -1){
                os.write(data,0,read);
            }
            zos.closeEntry();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            log.debug("获取分享文件失败："+"getShareFolder");
            e.printStackTrace();
        }

    }

    //查询文件并递归构建压缩文件结构
    private void buildCompressFolder(ZipOutputStream zipOutput,Integer parentId,Integer userId,String dir) throws IOException {
        List<FolderDTO> folders = udao.inquireUserFolders(parentId, userId, false);
        List<ResourceDTO> resource = rdao.inquireUserResourceByParentId(parentId, userId, false);

        ZipEntry entry = null;
        FolderDTO folderDTO = udao.inquireFolder(parentId);
        //创建当前级目录
        if(dir.equals("")){
            dir = folderDTO.getFolderName();
            //entry = new ZipEntry(folderDTO.getFolderName());
        }else {
           dir = dir+File.separator+folderDTO.getFolderName();
            //entry = new ZipEntry(dir+File.separator+folderDTO.getFolderName());
        }

        if(resource != null){
            //zipOutput.putNextEntry(entry);
            List<File> files = new ArrayList<>();
            int i=0;
            for(ResourceDTO e : resource){
                UserResource ur = urdao.inquireUserResourceById(e.getId());
                Resource re = rdao.inquireById(ur.getResourceId());
                File file = new File(re.getLocation());


                FileInputStream fis = new FileInputStream(file);
                //防止同名文件压缩失败 保留原文件名
                zipOutput.putNextEntry(new ZipEntry(dir+File.separator+(i++)+"_"+ur.getOriginalName()+re.getType().getType()));

                byte[] temp = new byte[1024];
                BufferedInputStream bis = new BufferedInputStream(fis);
                int read = 0;

                while((read = bis.read(temp) )!= -1){
                    zipOutput.write(temp,0,read);
                }

                bis.close();
                zipOutput.closeEntry();
                fis.close();
            }

            zipOutput.closeEntry();
        }


        if(folders != null){
            for(FolderDTO f : folders){
                buildCompressFolder(zipOutput,f.getFolderId(),userId,dir);
            }
        }

    }


    /**
     * 判断资源是否已经过期
     * @param createTime
     * @param survivalTime  有效时间，单位：分钟
     * @return
     */
    private boolean resourceIsExpire(Timestamp createTime, Integer survivalTime) {
        long time = createTime.getTime();
        long now = System.currentTimeMillis();

        return (time+(long)survivalTime*60*1000)-now < 0;
    }

    public boolean resourceIsExpire(String shareName) {
        boolean result = true;
        ShareResource sr = usrdao.inquireShareResourceBysName(shareName);
        if(sr != null){
            result = resourceIsExpire(sr.getCreateTime(),sr.getSurvivalTime());
        }

        return result;
    }
    //查询分片上传的进度

    /**
     * @Description: 文件已经未上传则去查询当前块的进度，否则直接返回上传成功
     * @author Turbine
     * @param
     * @param param
     * @return com.turbine.tnd.bean.Message
     * @date 2023/1/15 14:34
     */
    public Message inquireSliceProgress(FileRequestDTO param) {
        Message message = new Message(ResultCode.SUCCESS);

        if (!fs.hasExist(param.getFileName())) {
            List<Integer> list = supload.checkFinished(param);

            message.setData(list.stream().mapToInt(Integer::intValue).toArray());
        } else {
            FileUploadDTO fdto = new FileUploadDTO();
            fdto.setAccomplish(true);
            fdto.setAllSuccess(true);

            message.setData(fdto);
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

    public boolean FolderIsExist(int folderId) {
        return fdao.inquireFolderById(folderId) != null ;
    }

    public boolean hasFolder(int folderId , int userId) {
        return fdao.inquireFolder(folderId,userId) != null ;
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
     * 先进行逻辑删除后进行物理删除
     * @param userResourceId 用户资源id
     * @param model     删除模式 逻辑删除 0 ，物理删除 1
     * @param userName
     * @return
     */
    @Transactional
    public boolean delUserResource(Integer userResourceId, int model, String userName) {
        Message message = new Message();
        User user = udao.inquireByName(userName);
        message.setResultCode(ResultCode.ERROR_500);
        boolean result = false;

        if (user != null) {
                UserResource ur =  urdao.inquireUserResourceById(userResourceId);
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
                        result = true;
                    }
                }else if(model == 1){
                    if (rrdao.removeResourceRecycle(rr) > 0 && urdao.removeResource(ur) > 0) result = true;
                }

        }

            return result;
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
                Folder folder = fdao.inquireFolderById(folderId);

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
                        newRe.setResourceId(re.getId());
                        //newRe.setFileName(re.getFileId());
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

        public boolean recoverResource(Integer userResourceId) {
            boolean flag = false;
            UserResource ur = urdao.inquireUserResourceById(userResourceId);
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

            Folder parentFolder = fdao.inquireFolderById(folderId);
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
                Folder folder = fdao.inquireFolderById(folderId);
                flag = recoverFolder(folder,user.getId());
            }

            return flag;
        }

    /**
     *
     * @param srdto
     * @param userName
     * @return 生成分享id
     */
    public String createShareResource(ShareResourceDTO srdto ,String userName ) {
        String result = null;
        switch (srdto.getType()) {
            case 0:
                result = shareFolder(srdto,userName);
                break;
            case 1:
                result = shareFile(srdto,userName);
                break;

        }
        return result;
    }

    private String shareFolder(ShareResourceDTO srdto, String userName) {
        String re = null;
        User user = udao.inquireByName(userName);

        Folder folder = fdao.inquireFolder(srdto.getUserResourceId().intValue(), user.getId());
        if(folder != null){
            ShareResource sr = new ShareResource();
            String shareName = MD5Util.enryptionByKey(srdto.getOriginalName()+srdto.getUserResourceId(), userName);

            sr.setUserId(user.getId());
            sr.setUserResourceId(srdto.getUserResourceId());
            sr.setFetchCode(srdto.getFetchCode());
            sr.setCreateTime(new Timestamp(System.currentTimeMillis()));
            //sr.setOriginalName(folder.getFolderName());
            sr.setSurvivalTime(srdto.getSurvivalTime());
            sr.setShareName(shareName);
            sr.setType(0);
            if(usrdao.addShareResource(sr) > 0)re = shareName;
        }

        return re;
    }

    private String shareFile(ShareResourceDTO srdto ,String userName){
        String re = null;

        User user = udao.inquireByName(userName);
        if( hasResource(srdto.getUserResourceId(),user.getId()) ){
            //分享资源名
            String shareName = MD5Util.enryptionByKey(srdto.getOriginalName()+srdto.getUserResourceId(), userName);
            ShareResource sr = new ShareResource();
            UserResource ur = urdao.inquireUserResourceById(srdto.getUserResourceId());

            sr.setShareName(shareName);
            sr.setFetchCode(srdto.getFetchCode());
            sr.setUserId(user.getId());
            sr.setSurvivalTime(srdto.getSurvivalTime());
            sr.setUserResourceId(ur.getId());
            sr.setType(1);

            if(usrdao.addShareResource(sr) > 0)re = shareName;
        }

        return re;
    }


    /**
     *
     * @param userName
     * @return 用户已经分享的资源列表
     */
    public List<ShareResourceDTO> getShareResource(String userName) {

        List<ShareResourceDTO> list = null;
        User user = udao.inquireByName(userName);
        if(user != null){
            List<ShareResource> sr = usrdao.inquireShareResourceByUid(user.getId());
            if(sr != null && !sr.isEmpty()){
                list = new ArrayList<>();
                for(ShareResource entity : sr){
                    ShareResourceDTO srdto = new ShareResourceDTO();

                    if(entity.getType() == 0){
                        Folder folder = fdao.inquireFolderById(entity.getUserResourceId());
                        srdto.setType(0);
                        srdto.setOriginalName(folder.getFolderName());
                        srdto.setCreateTime(entity.getCreateTime());
                    }else if(entity.getType() == 1){
                        UserResource ur = urdao.inquireUserResourceById(entity.getUserResourceId());
                        if(ur != null){
                            srdto.setUserResourceId(ur.getId());
                            srdto.setType(1);
                            srdto.setCreateTime(entity.getCreateTime());
                        }
                    }
                    srdto.setFetchCode(entity.getFetchCode());
                    srdto.setSurvivalTime(entity.getSurvivalTime());
                    list.add(srdto);
                }
            }
        }

        return list;
    }



    public boolean undoShareResouce(String resourceName, String userName) {
        boolean result = false;
        User user = udao.inquireByName(userName);
        int sr = usrdao.delelteShareResourceByRName(resourceName,user.getId());
        if(sr > 0)result = true;
        return result;
    }


    public User getUser(String userName) {
        return udao.inquireByName(userName);
    }

    //保存用户资源
    /**
     * @Description: 将分享资源进行转存至指定的文件夹内操作
     * @author Turbine
     * @param
     * @param shareName 分享文定位hash id
     * @param parentId  转存至目的文件父文件id
     * @param userName    转存至指定用户
     * @return boolean
     * @date 2023/1/25 14:24
     */
    public boolean saveShareResource(String shareName,Integer parentId,String userName) {
        boolean flag = false;
        ShareResource shareResource = usrdao.inquireShareResourceBysName(shareName);
        User user = udao.inquireByName(userName);
        Integer userId = user.getId();

        if(shareResource != null){
            switch (shareResource.getType()){
                case 0: saveFolder(shareResource.getUserResourceId(), userId, parentId);break;
                case 1: {
                    UserResource userResource = urdao.inquireUserResourceById(shareResource.getUserResourceId());
                    saveFile(userResource, parentId,userId);
                    break;
                }
            }
            flag = true;
        }

        return flag;
    }

    private void saveFile(UserResource resource,Integer parentId,Integer userId) {
        resource.setU_id(userId);
        resource.setUploadTime(null);
        resource.setParentId(parentId);
        resource.setEncryption(false);
        resource.setEncryptPsw(null);
        resource.setS_flag(false);

        urdao.addUserResource(resource);
    }



    /**
     * @Description: 递归转存文件夹资源
     * @author Turbine
     * @param
     * @param folderId      原文件夹id
     * @param userId        目标用户id
     * @param parentId
     * @return void
     * @date 2023/1/25 14:33
     */
    private void saveFolder(Integer folderId,Integer userId,Integer parentId) {

        Folder folder = fdao.inquireFolderById(folderId);
        int originalU_id = folder.getUserId();
        folder.setFolderId(null);
        folder.setUserId(userId);
        folder.setCreateTime(null);
        folder.setParentId(parentId);
        folder.setS_flag(false);

        if(fdao.addFolder(folder) > 0){
            List<UserResource> resources = urdao.inquireUserResourceByParentId(folderId,originalU_id,false);
            for(UserResource resource : resources){
                //在该文件夹下继续存储资源文件
                saveFile(resource,folder.getFolderId(),userId);
            }
            List<FolderDTO> folders = udao.inquireUserFolders(folderId, originalU_id, false);
            for(FolderDTO f : folders){
                saveFolder(f.getFolderId(),userId,folder.getFolderId());
            }
        }

    }
}