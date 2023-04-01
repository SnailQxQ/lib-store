package com.turbine.tnd.service;

import com.sun.scenario.effect.impl.sw.java.JSWBrightpassPeer;
import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.*;
import com.turbine.tnd.utils.Filter;
import com.turbine.tnd.utils.FilterFactor;
import com.turbine.tnd.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/19 16:28
 */
@Service
@Slf4j
@SuppressWarnings("all")
public class UserService {
    @Autowired
    UserDao udao;
    @Autowired
    ResourceDao rdao;
    @Autowired
    FilterFactor filterFactor;

    /**
     * 通过记住我的方式登陆就验证登陆序列，否则就验证密码
     * 如果是通过密码登入 则也要更新登入序列sequence
     *
     * @param user     用户bean
     * @param remember 是否记住我
     * @param request
     * @return
     */
    public Message verify(User user, boolean remember, HttpServletRequest request) {
        boolean re = false;
        Message me = new Message();
        User result = null;
        UserDTO udto = new UserDTO();

        if (remember) {

            if ((result = udao.inquireBySequence(user)) != null) re = true;

        } else {
            //加密密码
            String s = MD5Util.saltEnryption(user.getPassword());
            user.setPassword(s);
            if ((result = udao.inquireByPsw(user)) != null) re = true;
            if (re) {
                //使用密码登陆成功后就更新登陆序列
                //得到更新后的新序列号
                String sequence = updateSequence(result.getId(), result.getUserName());
                if ("".equals(sequence)) re = false;
                else result.setSequence(sequence);
            }
        }

        if (re) {
            me.setCode(200);
            me.setMessage("success");
            //MD5Util.enryptionByKey(result.getUserName(),result.getPassword());
            String token = MD5Util.randomSaltEnryption(result.getUserName());

            udto.setToken(token);
            if (!remember) udto.setSequence(result.getSequence());
            udto.setUserName(result.getUserName());
            //登入成功 更新token
            request.getSession().setAttribute(udto.getUserName(), udto.getToken());
            //设置5小时没操作就过期
            request.getSession().setMaxInactiveInterval(18000);

            me.setData(udto);

        } else {
            me.setCode(400);
            me.setMessage("账号或密码输入错误！");
        }

        return me;
    }

    /**
     * 更新用户的登入序列号
     *
     * @param id 用户id
     * @return 更新后的登入序列，若更新失败则返回空串
     */
    private String updateSequence(int id, String userName) {
        User user = new User();

        String sequence = MD5Util.randomSaltEnryption(userName);
        user.setId(id);
        user.setSequence(sequence);

        int re = udao.updateUser(user);
        return re == 1 ? sequence : "";
    }

    public User getUser(String userName) {
        return udao.inquireByName(userName);
    }



    public boolean isExist(String userName){
        User user = udao.inquireByName(userName);
        return user != null;
    }

    public boolean addUser(User user) {
        boolean re = false;
        if(!isExist(user.getUserName())){
            String pwd = MD5Util.saltEnryption(user.getPassword());
            user.setPassword(pwd);
            if(udao.addUser(user) > 0) re = true;
        }
        return re;
    }


    public boolean uploadUserProfile(MultipartFile file, String userName) throws IOException {
        User user = udao.inquireByName(userName);
        user.setProfile(file.getBytes());
        return udao.updateUser(user) > 0;
    }
}