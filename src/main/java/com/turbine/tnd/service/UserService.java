package com.turbine.tnd.service;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.dao.UserDao;
import com.turbine.tnd.dto.UserDto;
import com.turbine.tnd.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/19 16:28
 */
@Service
public class UserService {
    @Autowired
    UserDao udao;

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

            if(( result = udao.selectBySequence(user) )!= null  )re = true;

        }else {
            //加密密码
            String s = MD5Util.saltEnryption(user.getPassword());
            user.setPassword(s);
            if((result = udao.selectByPsw(user) )!= null)re = true;
            if(re){
                //使用密码登陆成功后就更新登陆序列
                //得到更新后的新序列号
                String sequence = updateSequence(result.getId(),result.getUserName());
                result.setSequence(sequence);
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
     * @return
     */
    private String updateSequence(int id,String userName) {
        User user = new User();

        String sequence = MD5Util.randomSaltEnryption(userName);
        user.setId(id);
        user.setSequence(sequence);

        udao.updateUser(user);
        return sequence;
    }
}
