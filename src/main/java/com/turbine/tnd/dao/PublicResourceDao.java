package com.turbine.tnd.dao;

import com.turbine.tnd.bean.PublicResource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/6 14:32
 */
@Mapper
public interface PublicResourceDao {


    int addPublicResource(PublicResource pr);

    PublicResource inquirePublicResourceById(Integer id);

    int removeResourceById(Integer id);

    List<PublicResource> inquireUserPublicResource(int userId);

    List<PublicResource> inquireLikePublicResource(String name, Integer start, Integer size);

    List<PublicResource> inquireAllPublicResource(Integer start, Integer size);

    int modifyPublicResource(PublicResource pr);

    int incrCollectNum(Integer id);
}
