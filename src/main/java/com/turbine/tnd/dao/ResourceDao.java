package com.turbine.tnd.dao;

import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.bean.ResourceType;
import com.turbine.tnd.dto.ResourceDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
/**
 * @author Turbine
 * @Description
 * @date 2022/1/25 13:47
 */
@Mapper
public interface ResourceDao {
    /**
     * 添加资源并返回资源id
     * @param re
     * @return
     */
    int addResource(Resource re);

    /**
     *保存资源的的类型 resource_type表
     * @param fileName     资源名 uuid
     * @param type_id      资源类型id
     */
    void addReourceType(String fileName,int type_id);

    /**
     * 保存资源的上传者
     * @param u_id          用户id
     * @param fileName      资源名 uuid
     */
    void addResourceUser(int u_id,int resourceId,String fileName,String originalName,int parentId,int typeId);

    /**
     * 根据文件类型来查询类型文件id
     * @param suffix
     * @return
     */
    ResourceType inquireType(String suffix);

    /**
     * 根据文件名查询文件是否已经存在
     * @param fileName
     * @return
     */
    Resource inquireByName(String fileName);

    //查询指定用户指定文件夹下的指定资源
    List<ResourceDTO> inquireUserResourceByParentId(int parentId, int userId,Boolean isRecycle,Boolean isCollect);

    ResourceDTO inquireUserResourceByName(int userId,String fileName);
    //查询是否有该资源
    int hasResource(Integer userId, String fileName, Integer parentId);

    Resource inquireById(Integer fileId);
}
