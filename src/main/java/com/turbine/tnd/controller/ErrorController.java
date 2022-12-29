package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/9 22:38
 */
@CrossOrigin(origins = "*")
@RestController
public class ErrorController {

    @GetMapping("/error/tokenError")
    public Message errorTokenHandle(){
        return new Message(ResultCode.ERROR_401);
    }

    @GetMapping("/error/resourceNotFound")
    public Message resourceNotFound(){
        return new Message(ResultCode.ERROR_404);
    }
}
