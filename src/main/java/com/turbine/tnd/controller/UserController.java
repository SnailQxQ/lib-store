package com.turbine.tnd.controller;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dto.*;
import com.turbine.tnd.service.FileService;
import com.turbine.tnd.service.UserService;
import lombok.experimental.PackagePrivate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

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

    @GetMapping("/user/register/n/{userName}")
    public Message nameIsExist(@PathVariable("userName") String userName){
        boolean exist = us.isExist(userName);
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(exist);

        return message;
    }

    @PostMapping("/user/register")
    public Message register(@RequestBody User user){
        Message message = new Message(ResultCode.ERROR_500);
        if(!user.isValid())message.setResultCode(ResultCode.ERROR_400);
        else {
            if(us.addUser(user))message.setResultCode(ResultCode.SUCCESS);
        }
        return message;
    }

    @PostMapping("/user/login/{remember}")
    public Message login(@RequestBody User user
                        , @PathVariable("remember") boolean remember
                        , HttpServletRequest request
                        , HttpServletResponse response
                         ){
        Message verify = us.verify(user, remember, request);
        if(verify.getCode() == 200){
            Cookie token = new Cookie("token",((UserDTO)verify.getData()).getToken());
            Cookie userName = new Cookie("userName",((UserDTO)verify.getData()).getUserName());
            userName.setPath("/");
            token.setPath("/");

            token.setMaxAge(86400);
            userName.setMaxAge(86400);

            response.addCookie(token);
            response.addCookie(userName);

            if(!remember){
                Cookie sequence = new Cookie("sequence",((UserDTO)verify.getData()).getSequence());
                sequence.setPath("/");
                sequence.setMaxAge(86400);
                response.addCookie(sequence);
            }
        }
        return verify;
    }

    //根据用户名查询用户信息
    @GetMapping("/user/{name}")
    public Message getUserInfo(@PathVariable String name){
        User user = us.getUser(name);
        user.setSequence(null);
        user.setId(null);
        user.setPassword(null);
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(user);

        return message;
    }


    //上传头像
    @PostMapping("/user/profile")
    public Message uploadFile(MultipartFile file,@CookieValue String userName) throws IOException {
        Message message = new Message(ResultCode.ERROR_500);
        if(us.uploadUserProfile(file,userName))message.setResultCode(ResultCode.SUCCESS);

        return message;
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
