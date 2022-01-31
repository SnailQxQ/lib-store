package com.turbine.tnd.service;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResourceType;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.dao.UserDao;
import com.turbine.tnd.dto.FileUploadDTO;
import com.turbine.tnd.dto.FileUploadRequestDTO;
import com.turbine.tnd.dto.UserDto;
import com.turbine.tnd.utils.Filter;
import com.turbine.tnd.utils.FilterFactor;
import com.turbine.tnd.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/19 16:28
 */
@Service
@Slf4j
public class UserService {
    @Autowired
    UserDao udao;
    @Autowired
    FilterFactor filterFactor;
    @Autowired
    SimpleFileService fs;
    @Autowired
    @Qualifier("EruptUploadStrategy")
    EruptUploadStrategy supload;

    /**
     * 通过记住我的方式登陆就验证登陆序列，否则就验证密码
     * 如果是通过密码登入 则也要更新登入序列sequence
     * @param user      用户bean
     * @param remember  是否记住我
     * @param request
     * @return
     */
    public Message verify(User user, boolean remember, HttpServletRequest request) {
        boolean re = false;
        Message me = new Message();
        User result = null;
        UserDto udto = new UserDto();

        if(remember){

            if(( result = udao.inquireBySequence(user) )!= null  )re = true;

        }else {
            //加密密码
            String s = MD5Util.saltEnryption(user.getPassword());
            user.setPassword(s);
            if((result = udao.inquireByPsw(user) )!= null)re = true;
            if(re){
                //使用密码登陆成功后就更新登陆序列
                //得到更新后的新序列号
                String sequence = updateSequence(result.getId(),result.getUserName());
                if("".equals(sequence))re = false;
                else result.setSequence(sequence);
            }
        }

        if(re){
            me.setCode(200);
            me.setMessage("success");

            String token = MD5Util.enryptionByKey(result.getUserName(),result.getPassword());

            udto.setToken(token);
            if(!remember)udto.setSequence(result.getSequence());
            udto.setUserName(result.getUserName());
            //登入成功 更新token
            request.getSession().setAttribute(udto.getUserName(),udto.getToken());

            me.setData(udto);

        }else {
            me.setCode(400);
            me.setMessage("账号或密码输入错误！");
        }

        return me;
    }

    /**
     * 更新用户的登入序列号
     * @param id    用户id
     * @return  更新后的登入序列，若更新失败则返回空串
     */
    private String updateSequence(int id,String userName) {
        User user = new User();

        String sequence = MD5Util.randomSaltEnryption(userName);
        user.setId(id);
        user.setSequence(sequence);

        int re = udao.updateUser(user);
        return re == 1 ? sequence : "";
    }


    /**
     *
     * @param multipartFile
     * @param userName
     * @return
     */
    public boolean simpleUpload(MultipartFile multipartFile, String userName,Message message) {

        String originalName = multipartFile.getOriginalFilename();
        Filter<String> filter = filterFactor.getResource(FilterFactor.filterOpt.AC_FILTER);
        int idx = originalName.lastIndexOf(".");
        String suffix = originalName.substring(idx);
        String fileName = filter.filtration(originalName.substring(0,idx));

        System.out.println("文件名："+fileName);
        System.out.println("文件后缀："+suffix);

        //获取用户id 和类型id
         User user = udao.inquireByName(userName);
         ResourceType type = fs.inquireType(suffix);
         if(type == null){
             message.setResultCode(ResultCode.ERROR_400);
             message.setMessage("文件类型不支持！");
             return false;
         }

        return fs.upload(multipartFile,fileName,user.getId(),type.getId(),suffix);
    }

    /**
     * 分片上传dto
     *
     * @param fudto
     * @return
     */
    public Message sliceUpload(FileUploadRequestDTO fudto) {
        //
        Message message = new Message();

        if (!supload.isSupport(fudto.getFile().getOriginalFilename())) {
            message.setResultCode(ResultCode.ERROR_400);
            message.setMessage("文件类型不支持！");
            FileUploadDTO fdto = new FileUploadDTO();
            fdto.setAccomplish(false);
            fdto.setAllSuccess(false);
            log.debug(fudto.getFile().getOriginalFilename()+"文件类型不支持！");
            message.setData(message);
        }else if (fs.hasExist(fudto.getFileName())) {
            message.setResultCode(ResultCode.SUCCESS);
            FileUploadDTO fdto = new FileUploadDTO();
            fdto.setChunkNum(fudto.getChuckNum());
            fdto.setAccomplish(true);
            fdto.setAllSuccess(true);
            log.debug("文件已经在服务器有了！");
            message.setData(fdto);
        }else{
            message.setResultCode(ResultCode.SUCCESS);
            message.setData(supload.sliceUpload(fudto));
        }


        return message;
    }
}
