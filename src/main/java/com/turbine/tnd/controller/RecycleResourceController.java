package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.service.RecycleResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author Turbine
 * @Description:
 * @date 2023/2/16 16:17
 */
@CrossOrigin(origins = "*")
@RestController
public class RecycleResourceController {
    @Autowired
    RecycleResourceService s_rr;
    //查询回收站中的资源文件
    @GetMapping("/user/resource/recycle")
    public Message inquireRecycleResource(@CookieValue String userName){
        return s_rr.inquireRecycleResource(userName);
    }


    /**
     * 恢复文件夹
     * @return
     */
    @PutMapping("/user/resource/folder/rc/{folderId}")
    public Message recoverFolder(@PathVariable Integer folderId, @CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(s_rr.recoverFolder(folderId,userName))message.setResultCode(ResultCode.SUCCESS);

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
                && s_rr.recoverResource(userResourceId) )message.setResultCode(ResultCode.SUCCESS);

        return message;
    }

}
