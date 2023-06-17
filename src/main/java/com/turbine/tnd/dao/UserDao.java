package com.turbine.tnd.dao;

import com.turbine.tnd.bean.Folder;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.dto.FolderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/19 15:11
 */

@Mapper
public interface UserDao {
    //根据用户名和序列号查询
    User inquireBySequence(User user);
    //根据用户名和密码查询
    User inquireByPsw( User user);
    //根据id更新User信息
    int updateUser(User user);
    //根据用户名查询用户
    User inquireByName(String userName);
    //传入id时就根据指定id查询否则就查询全体
    User inquireById(@Param("userId")Integer userId);

    int addUser(User user);

}
