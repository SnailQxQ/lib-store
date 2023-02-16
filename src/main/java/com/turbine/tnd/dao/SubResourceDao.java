package com.turbine.tnd.dao;

import com.turbine.tnd.bean.SubResource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Turbine 加关注资源表
 * @Description:
 * @date 2023/2/6 21:56
 */
@Mapper
public interface SubResourceDao {


    int addSubResource(SubResource sr);

    int modifySubResource(SubResource sr);

    List<SubResource> inquireSubResource(Integer userId);
    //根据公共资源id查询收藏资源
    List<SubResource> inquireSubResourceByPId(Integer publicResourceId);

    SubResource inquireOwnSubResource(Integer publicResourceId, Integer userId);

    int removeSubResource(Integer id);
}
