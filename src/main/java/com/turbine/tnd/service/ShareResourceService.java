package com.turbine.tnd.service;

import com.turbine.tnd.bean.Folder;
import com.turbine.tnd.bean.ShareResource;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.bean.UserResource;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.FolderDTO;
import com.turbine.tnd.dto.ShareResourceDTO;
import com.turbine.tnd.utils.FilterFactor;
import com.turbine.tnd.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/16 16:30
 */

@Service
@Slf4j
@SuppressWarnings("all")
public class ShareResourceService {
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
    @Autowired
    UserShareResourceDao usrdao;

    @Autowired
    SubResourceDao srdao;

    @Autowired
    UserResourceService s_ur;

    @Value("${file.upload.tmpDir}")
    String tempDir;
    @Value("${file.upload.baseDir}")
    String baseDir;

    //打包下载指定分享文件
    /*public void getShareResource(String shareName, String userName, HttpServletResponse resp) throws IOException {
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
            usrdao.incrDowloads(shareName);
        }

    }*/

    /**
     * @Description: 下载用户分享的资源
     * @author Turbine
     * @param
     * @param shareName     分享hash名
     * @param resp          写入指定的响应流
     * @return void
     * @date 2023/2/4 16:49
     */
    public void getShareResource(String shareName, HttpServletResponse resp) throws IOException {
        ShareResource sr = usrdao.inquireShareResourceBysName(shareName);
        if(sr != null && !s_ur.resourceIsExpire(sr.getCreateTime(),sr.getSurvivalTime()) ){
            s_ur.getUResource(sr.getUserResourceId(), sr.getType(),resp,sr.getUserId());
            usrdao.incrDowloads(shareName);
        }
    }

    @Deprecated
    //下载的资源为文件夹形式需要进行压缩操作再发送（保持原有的文件结构）
    private void getShareFolder(ShareResource sr, Integer userId, OutputStream os) {
        int folderId = sr.getUserResourceId();
        List<FolderDTO> folderDTOS = udao.inquireUserFolders(folderId, userId, false,null);

        UserResource ur = urdao.inquireUserResourceById(sr.getUserResourceId());
        File zipF = new File(tempDir + File.separator + ur.getOriginalName() + ".zip");
        try(ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipF)))) {
            zos.setMethod(ZipOutputStream.DEFLATED);//zip格式

            s_ur.buildCompressFolder(zos,folderId,userId,"");
            byte[] data = new byte[1024];
            int read = 0;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipF));
            while( (read = bis.read(data) ) != -1){
                os.write(data,0,read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            log.debug("获取分享文件失败："+"getShareFolder");
            e.printStackTrace();
        }

    }

    public boolean resourceIsExpire(String shareName) {
        boolean result = true;
        ShareResource sr = usrdao.inquireShareResourceBysName(shareName);
        if(sr != null){
            result = s_ur.resourceIsExpire(sr.getCreateTime(),sr.getSurvivalTime());
        }

        return result;
    }


    /**
     *
     * @param srdto
     * @param userName
     * @return 生成分享id
     */
    public String createShareResource(ShareResourceDTO srdto , String userName ) {
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
            String shareName = MD5Util.randomSaltEnryption(srdto.getOriginalName()+srdto.getUserResourceId());

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
        if( s_ur.hasResource(srdto.getUserResourceId(),user.getId()) ){
            //分享资源名
            String shareName = MD5Util.randomSaltEnryption(srdto.getOriginalName()+srdto.getUserResourceId());
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


    private String getOriginalName(ShareResource usr){
        String originalName = null;
        switch (usr.getType()){
            case 0:{
                //资源为文件夹
                Folder folder = fdao.inquireFolderById(usr.getUserResourceId());
                originalName = folder.getFolderName();
                break;
            }
            case 1:{
                //文件夹类型资源
                UserResource userResource = urdao.inquireUserResourceById(usr.getUserResourceId());
                originalName = userResource.getOriginalName();
                break;
            }
        }
        return originalName;
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

                    srdto.assemble(entity);
                    srdto.setOriginalName(getOriginalName(entity));

                    list.add(srdto);
                }
            }
        }

        return list;
    }

    /**
     * @Description:
     * @author Turbine
     * @param
     * @param shareName 分享文件hash id
     * @return com.turbine.tnd.dto.ShareResourceDTO
     * @date 2023/1/26 14:54
     */
    public Object getOneShareResource(String shareName,String fetchCode) {

        ShareResource usr = usrdao.inquireShareResourceBysName(shareName);
        if(usr == null)return null;
        ShareResourceDTO srdto = new ShareResourceDTO();
        srdto.assemble(usr);
        if(!fetchCode.equals(srdto.getFetchCode()))return "提取码错误！";
        if(usr != null)srdto.assemble(usr);
        srdto.setOriginalName(getOriginalName(usr));
        usrdao.incrClicks(shareName);//点击次数++

        return srdto;
    }


    /**
     * @Description: 模糊查询指定用户的资源  //TODO:三表联查临时代替，要优化，太蠢了
     * @author Turbine
     * @param
     * @param userName
     * @param resourceName 资源名
     * @return com.turbine.tnd.dto.ShareResourceDTO
     * @date 2023/1/26 14:58
     */
    public List<ShareResourceDTO> getSimiliarityShareResource(String userName, String resourceName) {
        User user = udao.inquireByName(userName);
        List<ShareResourceDTO> re = new ArrayList<>();

        List<ShareResource> slist = usrdao.inquireShareResourceByUid(user.getId());
        for(ShareResource sr : slist){
            ShareResourceDTO dto = new ShareResourceDTO();
            switch (sr.getType()){
                case 0:{
                    Folder folder = fdao.inquireFolderById(sr.getUserResourceId());
                    if(folder.getFolderName().contains(resourceName)){
                        dto.assemble(sr);
                        dto.setOriginalName(getOriginalName(sr));
                        re.add(dto);
                    }
                    break;
                }
                case 1:{
                    UserResource usr = urdao.inquireUserResourceById(sr.getUserResourceId());
                    if(usr.getOriginalName().contains(resourceName)){
                        dto.assemble(sr);
                        dto.setOriginalName(getOriginalName(sr));
                        re.add(dto);
                    }
                    break;
                }
            }

        }

        return re;
    }

    public boolean undoShareResouce(String resourceName, String userName) {
        boolean result = false;
        User user = udao.inquireByName(userName);
        int sr = usrdao.delelteShareResourceByRName(resourceName,user.getId());
        if(sr > 0)result = true;
        return result;
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
                case 0: s_ur.saveFolder(shareResource.getUserResourceId(), userId, parentId);break;
                case 1: {
                    UserResource userResource = urdao.inquireUserResourceById(shareResource.getUserResourceId());
                    s_ur.saveFile(userResource, parentId,userId);
                    break;
                }
            }
            flag = true;
        }

        return flag;
    }

}
