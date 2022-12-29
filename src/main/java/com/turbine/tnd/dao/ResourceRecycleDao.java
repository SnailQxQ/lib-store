package com.turbine.tnd.dao;

import com.turbine.tnd.bean.ResourceRecycle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/3/28 19:09
 */
@Mapper
public interface ResourceRecycleDao {

    public int addResourceRecycle(ResourceRecycle rr);
    public int removeResourceRecycle(ResourceRecycle rr);
    //资源回收
    List<ResourceRecycle> inquireAll(@Param("u_id") int u_id);
}
