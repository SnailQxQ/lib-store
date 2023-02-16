package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/9 22:38
 */
@CrossOrigin(origins = "*")
@RestController
public class ErrorController {

    @RequestMapping("/error/tokenError")
    public Message errorTokenHandle(){
        return new Message(ResultCode.ERROR_401);
    }

    @RequestMapping("/error/resourceNotFound")
    public Message resourceNotFound(){
        return new Message(ResultCode.ERROR_404);
    }
}
