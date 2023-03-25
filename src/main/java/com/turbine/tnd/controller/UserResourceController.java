package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Folder;
import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.bean.UserResource;
import com.turbine.tnd.dto.FileRequestDTO;
import com.turbine.tnd.dto.RNavigationDTO;
import com.turbine.tnd.service.UserResourceService;
import io.netty.channel.MessageSizeEstimator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/16 16:18
 */
@CrossOrigin(origins = "*")
@RestController
public class UserResourceController {
    @Autowired
    UserResourceService s_ur;
    /**
     *
     * @param mfile
     * //@param user
     * @return
     *
     */
    @PostMapping("/user/resource/file/{model}")
    public Message uploadFile(MultipartFile[] mfile, @PathVariable("model")int model, @RequestParam("parentId") int parentId, @CookieValue String token, @CookieValue String userName){
        Message message = new Message();

        //简单上传
        if(model == 0){
            boolean flag = s_ur.simpleUpload(mfile[0],userName,message,parentId);
            if(flag){
                message.setResultCode(ResultCode.SUCCESS);
            }else {
                if(message.getCode() == 0)message.setResultCode(ResultCode.ERROR_400);
            }

        }else if(model == 1){
            //TODO:多文件上传

        }else {
            message.setResultCode(ResultCode.ERROR_400);
        }

        return message;
    }



    @PostMapping("/user/resource/file/slice")
    public Message sliceUpload(FileRequestDTO fudto, HttpServletRequest request) throws IOException {
        //防止上传用户名在中途被篡改，直接使用的cookie 中已经被验证过的
        Cookie[] cookies = request.getCookies();
        String userName = null;
        for(Cookie cookie : cookies){
            if( "userName".equals( cookie.getName() ) )userName = cookie.getValue();
        }
        fudto.setUserName(userName);


        return s_ur.sliceUpload(fudto);
    }

    /*@DeleteMapping("/user/undo/resource/{fileId}")
    public Message undoSliceUpload(@PathVariable String fileId,@CookieValue String userName){
        FileRequestDTO frdto = new FileRequestDTO();
        frdto.setFileName(fileId);
        frdto.setUserName(userName);
        return s_ur.undoSlicUpload(frdto);
    }*/


    /**
     * 查询文件上传进度
     */
    @GetMapping("/user/resource/file/progress/{fileName}")
    public Message inquireProgress(@PathVariable String fileName,@CookieValue("userName") String userName){
        FileRequestDTO frdto = new FileRequestDTO();
        frdto.setUserName(userName);
        frdto.setFileName(fileName);
        return s_ur.inquireSliceProgress(frdto);
    }

    //查询指定用户的指定文件夹下的所有文件 ，不展示已经被删除的文件
    @GetMapping("/user/resource/{parentId}")
    public Message inquireUserFile(@PathVariable("parentId") int parentId,@CookieValue String userName){
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(s_ur.getOwnLevelResource(parentId,userName,null));
        return message;
    }

    //查询指定用户的指定文件夹下的所有文件 ，不展示已经被删除的文件
    @GetMapping("/user/resource/collet/{parentId}")
    public Message inquireCollectUserFile(@PathVariable("parentId") int parentId,@CookieValue String userName){
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(s_ur.getOwnLevelResource(parentId,userName,true));
        return message;
    }

    //TODO:资源是否持有验证放入拦截器里面去执行
    @PostMapping("/user/resource/collet")
    public Message addCollectResource(@RequestBody UserResource userResource,@CookieValue String userName){
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(s_ur.addCollectResource(userResource.getResourceId(),userResource.getTypeId(),userName));
        return  message;
    }



    //创建文件夹
    @PostMapping("/user/resource/folder/{parentId}")
    public Message mkdirFolder(@PathVariable("parentId") int parentId,@CookieValue String userName,String folderName){
        if(folderName == null)return new Message(ResultCode.ERROR_400);

        return s_ur.mkdirFolder(parentId,userName,folderName);
    }
    //更新文件夹
    @PutMapping("/user/resource/folder/{folderId}")
    public Message modifyFolder(@PathVariable("folderId") int folderId,@CookieValue("userName")String userName, @RequestBody Folder folder){
        folder.setFolderId(folderId);
        return s_ur.modifyFolder(folder,userName);
    }

    //未使用
    @Deprecated
    @GetMapping("/user/resource/file/slice/{fileId}")
    public Message sliceDownload(@PathVariable("fileId") String fileId,@RequestBody FileRequestDTO fdto){
        if(fdto == null)return new Message(ResultCode.ERROR_400);

        fdto.setFileName(fileId);
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(s_ur.sliceDownload(fdto));
        return message;
    }

    //resourceId 用户资源id
    @PutMapping("/user/resource/file/{resourceId}")
    public Message modifyUserResource(@PathVariable("resourceId") Integer userResourceId, @CookieValue("userName")String userName,@RequestBody UserResource uResource){
        if(userResourceId == null )return  new Message(ResultCode.ERROR_400);

        uResource.setId(userResourceId);
        return s_ur.modifyUserResource(uResource,userName);
    }

    //resourceId 用户资源id
    @GetMapping("/user/resource/file/{type}/{resourceId}")
    public void downLoadUserFile(@PathVariable Map map, HttpServletResponse resp, @CookieValue String userName){
        String type = (String) map.get("type");
        String userResourceId = (String)map.get("resourceId");
        try {
            s_ur.getResource(Integer.parseInt(userResourceId),resp,Integer.parseInt(type),userName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @GetMapping("/user/resource/folder/status/{folderId}")
    public Message getFolderIsEmpty(@PathVariable("folderId") Integer folderId,@CookieValue String userName){

        boolean re = s_ur.getFolderIsEmpty(folderId,userName);
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(re);
        return message;
    }

    /**
     * @param： fileId资源id
     *          model 删除模式 0标记删除，1 物理删除
     * @return
     */
    @DeleteMapping("/user/resource/file/{resourceId}/{model}")
    public Message delUserResouce(@PathVariable Map PathVariableMap ,@CookieValue String userName){
        String userResourceId = (String)PathVariableMap.get("resourceId");
        String model = (String)PathVariableMap.get("model");
        Message message = new Message(ResultCode.ERROR_500);

        if(userResourceId == null || model == null || (!"0".equals(model) && !"1".equals(model)))message.setResultCode(ResultCode.ERROR_400);
        if(s_ur.delUserResource(Integer.parseInt(userResourceId),Integer.parseInt(model),userName))message.setResultCode(ResultCode.SUCCESS);

        return message;
    }

    /**
     *
     * @param PathVariableMap
     * @param userName
     *         model : 0 为逻辑删除，1 为物理删除
     * @return
     */
    @DeleteMapping("/user/resource/folder/{folderId}/{model}")
    public Message delUserFolder(@PathVariable Map PathVariableMap,@CookieValue String userName){
        String folderId = (String)PathVariableMap.get("folderId");
        String model = (String)PathVariableMap.get("model");

        if(folderId == null || model == null || (!"0".equals(model) && !"1".equals(model)))return new Message(ResultCode.ERROR_400);
        return s_ur.delUserFolder(Integer.parseInt(folderId),userName,"1".equals(model));
    }



    @GetMapping("/user/resource/location/{folderId}")
    public Message getLocation(@PathVariable Integer folderId, @CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        RNavigationDTO data = s_ur.getRLocation(folderId);
        if(data != null){
            message.setResultCode(ResultCode.SUCCESS);
            message.setData(data);
        }else message.setResultCode(ResultCode.ERROR_404);

        return message;
    }

    //TODO:测试视频
    @GetMapping("/user/resource/video/{id}")
    public Message getLocation(@PathVariable Integer id){
        Message message = new Message(ResultCode.SUCCESS);
        Object data = s_ur.getMovieLocation(id);
       if(data == null)message.setResultCode(ResultCode.ERROR_404);
       else {
            message.setData(data);
       }

       return message;
    }



}
