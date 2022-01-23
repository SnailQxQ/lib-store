package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.service.UserService;
import com.turbine.tnd.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/19 16:21
 */

@RestController()
public class UserController {
    @Autowired
    UserService us;

    @PostMapping("/login/{remember}")
    public Message login(@RequestBody User user, @PathVariable("remember") boolean remember, HttpServletRequest request){
        Message me = new Message();
       // System.out.println(user.toString());
       // System.out.println(remember);
        return  us.verify(user, remember, request);
    }
}
