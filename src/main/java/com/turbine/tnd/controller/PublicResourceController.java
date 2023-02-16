package com.turbine.tnd.controller;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.dto.PublicResourceDTO;
import com.turbine.tnd.service.PublicResourceService;
import com.turbine.tnd.service.UserResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Turbine
 * @Description:    管理公共资源
 * @date 2023/2/16 16:14
 */
@CrossOrigin(origins = "*")
@RestController
public class PublicResourceController {

    @Autowired
    PublicResourceService s_pr;
    @Autowired
    UserResourceService s_ur;

    @PostMapping("/user/resource/shared")
    public Message createPublicResource(@RequestBody PublicResourceDTO pr, @CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(pr.getUserResourceId() == null || pr.getType() == null || (pr.getType() != 0 && pr.getType() != 1)){
            message.setResultCode(ResultCode.ERROR_400);
        }else {
            if(s_pr.createPublicResource(pr.getName(),pr.getUserResourceId(),pr.getType(),pr.getIntro(),userName))message.setResultCode(ResultCode.SUCCESS);
            message.setData(true);
        }
        return message;
    }

    //查询分享资源文件夹下的所有文件 ，不展示已经被删除的文件 parentId 父文件夹id
    @GetMapping("/user/resource/shared/detail/{parentId}")
    public Message getPublicFile(@PathVariable("parentId") int parentId, @CookieValue String userName){
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(s_ur.getLevelResource(parentId));
        return message;
    }

    @DeleteMapping("/user/resource/shared/{publicResourceId}")
    public Message undoPublicResource(@PathVariable Integer publicResourceId ,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);

        if(publicResourceId == null)message.setResultCode(ResultCode.ERROR_400);
        else if(s_pr.deletePublicResource(publicResourceId,userName)) message.setResultCode(ResultCode.SUCCESS);

        return message;
    }

    //获取自己共享的资源
    @GetMapping(value = {"/user/resource/shared/{publicResourceId}","/user/resource/shared"})
    public Message  getPublicResource(@PathVariable(required = false) Integer publicResourceId,@CookieValue String userName){
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(s_pr.getOwnPublicResource(publicResourceId,userName));
        return message;
    }


    //查询共享资源列表
    @GetMapping(value = {"/user/resource/shared/public/{name}","/user/resource/shared/public"})
    public Message getPublicResource(@PathVariable(required = false)String name,@RequestParam Integer size,@RequestParam Integer page){
        Message message = new Message(ResultCode.SUCCESS);
        message.setData(s_pr.getPublicResource(name,size,page));
        return message;
    }

    //转存公共资源
    @PostMapping("/user/resource/shared/public/transfer")
    public Message transferPublicResource(@RequestBody PublicResourceDTO dto,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(dto.getId() == null || dto.getParentId() == null)message.setResultCode(ResultCode.ERROR_400);
        else {
            if( s_pr.savePublicResource(dto.getId(),userName,dto.getParentId()) ) message.setResultCode(ResultCode.SUCCESS);
        }

        return message;
    }



    @PostMapping("/user/resource/shared/public/sub")
    public Message addSubResource(@RequestBody PublicResourceDTO dto, @CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(dto.getId() == null)message.setResultCode(ResultCode.ERROR_400);
        else {
            if( s_pr.addSubResource(dto.getId(),userName) ) message.setResultCode(ResultCode.SUCCESS);
        }
        return message;
    }

    @GetMapping("/user/resource/sub/")
    public Message getSubResource(@CookieValue String userName){
        Message me = new Message(ResultCode.SUCCESS);

        me.setData(s_pr.getSubResource(userName));
        return me;
    }

    @DeleteMapping("/user/resource/sub/{id}")
    public Message delSubResource(@PathVariable Integer id,@CookieValue String userName){
        Message message = new Message(ResultCode.ERROR_500);
        if(id == null)message.setResultCode(ResultCode.ERROR_400);
        else if( s_pr.delSubResource(id) ) message.setResultCode(ResultCode.SUCCESS);

        return message;
    }
}
