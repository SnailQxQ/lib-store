package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.dto.FileUploadDTO;
import com.turbine.tnd.dto.FileUploadRequestDTO;
import com.turbine.tnd.dto.UserDto;
import com.turbine.tnd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/19 16:21
 */

@RestController
public class UserController {
    @Autowired
    UserService us;


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
            Cookie token = new Cookie("token",((UserDto)verify.getData()).getToken());
            Cookie userName = new Cookie("userName",((UserDto)verify.getData()).getUserName());
            userName.setPath("/");
            token.setPath("/");

            response.addCookie(token);
            response.addCookie(userName);

            if(!remember){
                Cookie sequence = new Cookie("sequence",((UserDto)verify.getData()).getSequence());
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
    @PostMapping("/user/upload/{model}")
    public Message uploadFile(MultipartFile[] mfile,@PathVariable("model")int model, @CookieValue String token,@CookieValue String userName){
        Message message = new Message();

        //简单上传
        if(model == 0){
            boolean flag = us.simpleUpload(mfile[0],userName,message);
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

    @PostMapping("/user/sliceUpload")
    public Message sliceUpload(FileUploadRequestDTO fudto){
        System.out.println(fudto);
        return us.sliceUpload(fudto);
    }



}
