package com.turbine.tnd.dao;

import com.turbine.tnd.bean.UserResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/14 23:54
 */
@Mapper
public interface UserResourceDao {
    int modifyResource(UserResource uResource);
    //根据用户id 和资源id删除
    int removeResource(UserResource ur);

    UserResource inquireUserResourceByName(@Param("u_id") int u_id, @Param("fileId") String fileId,@Param("parentId")Integer parentId);

    UserResource inquireUserResourceById(Integer userResourceId);
    //根据资源id进行查询
    List<UserResource> inquireUserResourceByrid(Integer resourceId);

    int addUserResource(UserResource ur);

    UserResource inquireUserResourceByName(@Param("u_id")int id, @Param("fileId")String fileName, @Param("fileName")String originalName, @Param("parentId")Integer parentId);

    int countUserResource(int resourceId, int userId);
}
