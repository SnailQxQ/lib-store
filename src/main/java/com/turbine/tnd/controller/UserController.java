package com.turbine.tnd.controller;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dto.FileRequestDTO;
import com.turbine.tnd.dto.ShareResourceDTO;
import com.turbine.tnd.dto.UserDTO;
import com.turbine.tnd.service.FileService;
import com.turbine.tnd.service.UserService;
import com.turbine.tnd.utils.ZipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Turbine
 * @Description : http://81.68.90.126:8888
 * @date 2022/1/19 16:21
 */
@CrossOrigin(origins = "*")
@RestController
public class UserController {
    @Autowired
    UserService us;
    @Autowired
    FileService fs;
    @Value("${file.upload.baseDir}")
    String baseDir;


    @PostMapping("/user/login/{remember}")
    public Message login(@RequestBody User user
                        , @PathVariable("remember") boolean remember
                        , HttpServletRequest request
                        , HttpServletResponse response
                         ){
       // System.out.println(user.toString());
       // System.out.println(remember);
        Message verify = us.verify(user, remember, request);
        if(verify.getCode() == 200){
            Cookie token = new Cookie("token",((UserDTO)verify.getData()).getToken());
            Cookie userName = new Cookie("userName",((UserDTO)verify.getData()).getUserName());
            userName.setPath("/");
            token.setPath("/");

            response.addCookie(token);
            response.addCookie(userName);

            if(!remember){
                Cookie sequence = new Cookie("sequence",((UserDTO)verify.getData()).getSequence());
                sequence.setPath("/");
                response.addCookie(sequence);
            }
        }
        return verify;
    }

    /**
     *
     * @param mfile
     * //@param user
     * @return
     *
     */
    @PostMapping("/user/resource/file/{model}")
    public Message uploadFile(MultipartFile[] mfile,@PathVariable("model")int model,@RequestParam("parentId") int parentId, @CookieValue String token,@CookieValue String userName){
        Message message = new Message();

        //简单上传
        if(model == 0){
            boolean flag = us.simpleUpload(mfile[0],userName,message,parentId);
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
    public Message sliceUpload(FileRequestDTO fudto, HttpServletRequest request){
        //防止上传用户名在中途被篡改，直接使用的cookie 中已经被验证过的
        Cookie[] cookies = request.getCookies();
        String userName = null;
        for(Cookie cookie : cookies){
            if( "userName".equals( cookie.getName() ) )userName = cookie.getValue();
        }
        fudto.setUserName(userName);


        return us.sliceUpload(fudto);
    }

    @DeleteMapping("/user/undo/resource/{fileId}")
    public Message undoSliceUpload(@PathVariable String fileId,@CookieValue String userName){
        FileRequestDTO frdto = new FileRequestDTO();
        frdto.setFileName(fileId);
        frdto.setUserName(userName);
        return fs.undoSlicUpload(frdto);
    }


    /**
     * 查询文件上传进度
     */
    @GetMapping("/user/resource/file/progress/{fileName}")
    public Message inquireProgress(@PathVariable String fileName,@CookieValue("userName") String userName){
        FileRequestDTO frdto = new FileRequestDTO();
        frdto.setUserName(userName);
        frdto.setFileName(fileName);
       return us.inquireSliceProgress(frdto);
    }

    //查询指定用户的指定文件夹下的所有文件 ，不展示已经被删除的文件
    @GetMapping("/user/resource/{parentId}")
    public Message inquireUserFile(@PathVariable("parentId") int parentId,@CookieValue String userName){
        return us.inquireOwnFile(parentId,userName);
    }
    //查询回收站中的资源文件
    @GetMapping("/user/resource/recycle")
    public Message inquireRecycleResource(@CookieValue String userName){
        return us.inquireRecycleResource(userName);
    }

    //创建文件夹
    @PostMapping("/user/resource/folder/{parentId}")
    public Message mkdirFolder(@PathVariable("parentId") int parentId,@CookieValue String userName,String folderName){
        if(folderName == null)return new Message(ResultCode.ERROR_400);

        return us.mkdirFolder(parentId,userName,folderName);
    }
    //更新文件夹
    @PutMapping("/user/resource/folder/{folderId}")
    public Message modifyFolder(@PathVariable("folderId") int folderId,@CookieValue("userName")String userName, @RequestBody  Folder folder){
        folder.setFolderId(folderId);
        return us.modifyFolder(folder,userName);
    }

    //未使用
    @Deprecated
    @GetMapping("/user/resource/file/slice/{fileId}")
    public Message sliceDownload(@PathVariable("fileId") String fileId,@RequestBody FileRequestDTO fdto){
        if(fdto == null)return new Message(ResultCode.ERROR_400);

        fdto.setFileName(fileId);
        return us.sliceDownload(fdto);
    }

    //resourceId 用户资源id
    @PutMapping("/user/resource/file/{resourceId}")
    public Message modifyUserResource(@PathVariable("resourceId") Integer userResourceId, @CookieValue("userName")String userName,@RequestBody UserResource uResource){
        if(userResourceId == null )return  new Message(ResultCode.ERROR_400);

        uResource.setId(userResourceId);
        return us.modifyUserResource(uResource,userName);
    }

    //resourceId 用户资源id
    @GetMapping("/user/resource/file/{resourceId}")
    public ResponseEntity<byte[]> downLoadUserFile(@PathVariable("resourceId") Integer userResourceId){

        try {

            Resource resource = us.inquireResource(userResourceId);
            File file = new File(resource.getLocation());
            byte[] body = null;
            InputStream is = new FileInputStream(file);
            body = new byte[is.available()];
            is.read(body);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attchement;filename=" +resource.getFileName()+resource.getType().getType());
            ResponseEntity<byte[]> entity = new ResponseEntity<byte[]>(body, headers, HttpStatus.OK);

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        if(us.delUserResource(Integer.parseInt(userResourceId),Integer.parseInt(model),userName))message.setResultCode(ResultCode.SUCCESS);

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
        return us.delUserFolder(Integer.parseInt(folderId),userName,"1".equals(model));
    }

    /**
     * 恢复文件夹
     * @return
     */
    @PutMapping("/user/resource/folder/rc/{folderId}")
    public Message recoverFolder(@PathVariable Integer folderId,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(us.recoverFolder(folderId,userName))message.setResultCode(ResultCode.SUCCESS);

        return message;
    }
    /**
     * 恢复资源
     * @return
     */
    @PutMapping("/user/resource/file/rc/{userResourceId}")
    public Message recoverResource(@PathVariable Integer userResourceId,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(userResourceId != null
                && us.recoverResource(userResourceId) )message.setResultCode(ResultCode.SUCCESS);

        return message;
    }



    //生成分享文件和文件提取码
    @PostMapping("/user/resource/share")
    public Message createShareResource(@RequestBody ShareResourceDTO srdto, @CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        String shareId;
        if(!srdto.isValid())message.setResultCode(ResultCode.ERROR_400);
        else if( (shareId = us.createShareResource(srdto,userName) )!= null){
            message.setResultCode(ResultCode.SUCCESS);
            message.setData(shareId);
        }
        return message;
    }

    //查询用户分享的资源
    @GetMapping("/user/resource/share")
    public Message inquireShareResource(@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(userName != null){
            List<ShareResourceDTO> shareList = us.getShareResource(userName);
            message.setResultCode(ResultCode.SUCCESS);
            message.setData(shareList);
        }
        return message;
    }
    //取消分享
    @DeleteMapping("/user/resource/share/{resourceName}")
    public Message undoShareResouce(@PathVariable String resourceName,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);

        if(userName != null && us.undoShareResouce(resourceName,userName)){
            message.setResultCode(ResultCode.SUCCESS);
        }

        return message;
    }


    @GetMapping("/user/resource/file/s/{shareName}")
    public void dowloadShareResouce(@PathVariable String shareName,@CookieValue String userName,HttpServletResponse resp) throws IOException {
        us.getShareResource(shareName,userName,resp);
    }

    //检查资源是否过期
    @GetMapping("/user/resource/file/expire/{shareName}")
    public Message shareResouceIsExpire(@PathVariable String shareName){
        Message me = new Message(ResultCode.SUCCESS);
        me.setData(us.resourceIsExpire(shareName));
        return me;
    }


    @PutMapping("/user/resource/share/transfer/{shareName}")
    public Message transferResource(@PathVariable String shareName,Integer parentId,@CookieValue String userName){
        Message me = new Message(ResultCode.ERROR_500);
        if(parentId == null || shareName == null)me.setResultCode(ResultCode.ERROR_400);
        else{

            if(us.saveShareResource(shareName,parentId,userName)) me.setResultCode(ResultCode.SUCCESS);
        }

        return me;
    }

    /* @GetMapping("/test/getZip")
    public void getZip(HttpServletResponse resp) throws IOException {
        resp.addHeader("Content-Disposition", "attchement;filename=testNewZip.zip");
        ServletOutputStream os = resp.getOutputStream();
        List<File> files = new ArrayList<>();
        File file1 = new File("C:\\Users\\turbine\\Desktop\\tnd.sql");
        File file2 = new File("C:\\Users\\turbine\\Desktop\\新建文本文档.txt");
        files.add(file1);
        files.add(file2);
        ZipUtils.compressZip(files,os);
    }*/
}
