package com.turbine.tnd.controller;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dto.FileRequestDTO;
import com.turbine.tnd.dto.UserDTO;
import com.turbine.tnd.service.FileService;
import com.turbine.tnd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
    public Message inquireRecycleBin(@CookieValue String userName){
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

    @GetMapping("/user/resource/file/slice/{fileId}")
    public Message sliceDownload(@PathVariable("fileId") String fileId,@RequestBody FileRequestDTO fdto){
        if(fdto == null)return new Message(ResultCode.ERROR_400);

        fdto.setFileName(fileId);
        return us.sliceDownload(fdto);
    }


    @PutMapping("/user/resource/file/{resourceId}")
    public Message modifyUserResource(@PathVariable("resourceId") String fileId, @CookieValue("userName")String userName,@RequestBody UserResource uResource){
        if(fileId == null )return  new Message(ResultCode.ERROR_400);

        uResource.setId(Integer.parseInt(fileId));
        return us.modifyUserResource(uResource,userName);
    }

    //需要验证此文件资源是否是当前用户可拥有的
    @GetMapping("/user/resource/file/{fileId}")
    public ResponseEntity<byte[]> downLoad(@PathVariable("fileId") String fileId){

        try {
            Resource resource = us.inquireResource(fileId);
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
    @DeleteMapping("/user/resource/file/{parentId}/{resourceId}/{model}")
    public Message delUserResouce(@PathVariable Map PathVariableMap ,@CookieValue String userName){
        String resourceId = (String)PathVariableMap.get("resourceId");
        String parentId = (String)PathVariableMap.get("parentId");
        String model = (String)PathVariableMap.get("model");
        if(resourceId == null || model == null || parentId == null || (!"0".equals(model) && !"1".equals(model)))return new Message(ResultCode.ERROR_400);

        return us.delUserResource(Integer.parseInt(resourceId),Integer.parseInt(model),userName,Integer.parseInt(parentId));
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
    @PutMapping("/user/resource/file/rc/{resourceId}")
    public Message recoverResource(@PathVariable Integer resourceId,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(resourceId != null
                && us.recoverResource(resourceId) )message.setResultCode(ResultCode.SUCCESS);

        return message;
    }







}
