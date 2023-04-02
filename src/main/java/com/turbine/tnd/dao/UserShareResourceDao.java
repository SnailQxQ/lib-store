package com.turbine.tnd.dao;

import com.turbine.tnd.bean.ShareResource;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.Mapping;

import java.util.List;

/**
 * @author Turbine
 * @Description     用户资源表dao
 * @date 2022/3/28 15:14
 */
@Mapper
public interface UserShareResourceDao {

    public int modifyShareResource(ShareResource sr);

    public int addShareResource(ShareResource sr);

    List<ShareResource> inquireShareResourceByUid(int userId);

    int delelteShareResourceByRName(String shareName, int userId);

    ShareResource inquireShareResourceBysName(String shareName);

    //模糊查询我的分享
    //List<ShareResource> inquireShareResourceBySSName(int userId, String shareName);

    int incrClicks(String shareName);

    int incrDowloads(String shareName);

    int delelteShareResourceByURId(int id);

    //根据用户资源id查询当前用户的分享资源
    List<ShareResource> inquireShareResourceById(Integer userId, Integer userResourceId);
}
