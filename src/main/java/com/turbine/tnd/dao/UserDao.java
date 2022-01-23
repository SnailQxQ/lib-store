package com.turbine.tnd.dao;

import com.turbine.tnd.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/19 15:11
 */

@Mapper
public interface UserDao {
    //根据用户名和序列号查询
    User selectBySequence( User user);
    //根据用户名和密码查询
    User selectByPsw( User user);
    //根据id更新User信息
    void updateUser(User user);
}
