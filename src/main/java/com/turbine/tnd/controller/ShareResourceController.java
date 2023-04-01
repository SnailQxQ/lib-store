package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.dto.ShareResourceDTO;
import com.turbine.tnd.service.ShareResourceService;
import com.turbine.tnd.service.UserResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/16 16:20
 */
@CrossOrigin(origins = "*")
@RestController
public class ShareResourceController {

    @Autowired
    ShareResourceService s_sr;
    @Autowired
    UserResourceService s_ur;

    //生成分享文件和文件提取码
    @PostMapping("/user/resource/share")
    public Message createShareResource(@RequestBody ShareResourceDTO srdto, @CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        String shareId;
        if(!srdto.verify())message.setResultCode(ResultCode.ERROR_400);
        else if( (shareId = s_sr.createShareResource(srdto,userName) )!= null){
            message.setResultCode(ResultCode.SUCCESS);
            message.setData(shareId);
        }
        return message;
    }


    //查询分享指定文件夹下的所有文件 ，不展示已经被删除的文件
    @GetMapping("/user/resource/share/detail/{parentId}")
    public Message inquireShareFile(@PathVariable("parentId") Integer parentId, @CookieValue String userName){
        Message message = new Message(ResultCode.SUCCESS);
        if(parentId != null){
            message.setData(s_ur.getLevelResource(parentId));
        }
        return message;
    }

    //查询用户分享的资源
    @GetMapping("/user/resource/share")
    public Message inquireShareResource(@CookieValue(required=false) String userName,@RequestParam(required=false) String name,
                                        @RequestParam("type") Integer type, @RequestParam(required=false) String fetchCode){
        Message message = new Message(ResultCode.ERROR_500);

        Object data = null;
        switch (type){
            case 0:{
                //查询当前用户的全部资源
                if(userName == null){
                    message.setResultCode(ResultCode.ERROR_401);
                    return message;
                }
                else data  = s_sr.getShareResource(userName);
                break;
            }
            case 1:{
                //查询指定用户资源
                if(name != null)data = s_sr.getOneShareResource(name,fetchCode);
                else message.setResultCode(ResultCode.ERROR_400);
                break;
            }
            case 2:{
                //模糊查询指定用户资源
                if(userName == null){
                    message.setResultCode(ResultCode.ERROR_401);
                    return message;
                }
                if(name != null)data = s_sr.getSimiliarityShareResource(userName,name);
                else message.setResultCode(ResultCode.ERROR_400);

                break;
            }
        }

        if(data != null){
            message.setData(data);
            message.setResultCode(ResultCode.SUCCESS);
        }else message.setResultCode(ResultCode.ERROR_404);


        return message;
    }
    //取消分享
    @DeleteMapping("/user/resource/share/{resourceName}")
    public Message undoShareResouce(@PathVariable String resourceName,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);

        if(userName != null && s_sr.undoShareResouce(resourceName,userName)){
            message.setResultCode(ResultCode.SUCCESS);
        }

        return message;
    }


    @GetMapping("/user/resource/file/s/{shareName}")
    public void dowloadShareResouce(@PathVariable String shareName, HttpServletResponse resp) throws IOException {
        s_sr.getShareResource(shareName,resp);
    }

    @PostMapping("/user/resource/share/transfer/{shareName}")
    public Message transferResource(@PathVariable String shareName,Integer parentId,@CookieValue String userName){
        Message me = new Message(ResultCode.ERROR_500);
        if(parentId == null || shareName == null)me.setResultCode(ResultCode.ERROR_400);
        else{

            if(s_sr.saveShareResource(shareName,parentId,userName)) me.setResultCode(ResultCode.SUCCESS);
        }

        return me;
    }

    //检查资源是否过期
    @GetMapping("/user/resource/file/expire/{shareName}")
    public Message shareResouceIsExpire(@PathVariable String shareName){
        Message me = new Message(ResultCode.SUCCESS);
        me.setData(s_sr.resourceIsExpire(shareName));
        return me;
    }
}
