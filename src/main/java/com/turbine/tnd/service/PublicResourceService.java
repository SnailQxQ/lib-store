package com.turbine.tnd.service;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.*;
import com.turbine.tnd.dto.PublicResourceDTO;
import com.turbine.tnd.utils.Filter;
import com.turbine.tnd.utils.FilterFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Turbine
 * @Description:
 * @date 2023/2/16 16:25
 */
@Service
@Slf4j
@SuppressWarnings("all")
public class PublicResourceService {
    @Autowired
    UserDao udao;
    @Autowired
    ResourceDao rdao;
    @Autowired
    PublicResourceDao prdao;
    @Autowired
    FilterFactor filterFactor;
    @Autowired
    FolderDao fdao;
    @Autowired
    UserResourceDao urdao;
    @Autowired
    SubResourceDao srdao;

    @Autowired
    UserResourceService s_ur;











    /**
     * @Description: 创建共享资源
     * @author Turbine
     * @param
     * @param userResourceId
     * @param type              资源类型，0：文件夹 1： 文件
     * @param intro
     * @param userName
     * @return boolean
     * @date 2023/2/6 15:45
     */
    public boolean createPublicResource(String name,Integer userResourceId, Integer type, String intro ,String userName) {
        boolean re = false;
        User user = udao.inquireByName(userName);
        Filter<String> filter = filterFactor.getResource(FilterFactor.filterOpt.AC_FILTER);

        if(type == 0){
            Folder folder = fdao.inquireFolderById(userResourceId);
            folder.setS_flag(true);
            fdao.modifyFolder(folder);
        }else if(type == 1){
            UserResource ur = urdao.inquireUserResourceById(userResourceId);
            ur.setS_flag(true);
            urdao.modifyResource(ur);
        }
        PublicResource  pr = new PublicResource(name, filter.filtration(intro),userResourceId,type,0,0,user.getId());
        if(prdao.addPublicResource(pr) > 0){
            re = true;
        }
        return re;
    }


    /**
     * @Description:    取消共享资源 //TODO:还没有加收藏的文件进行删除标记
     * @author Turbine
     * @param
     * @param publicResourceId
     * @param userName
     * @return boolean
     * @date 2023/2/6 16:56
     */
    public boolean deletePublicResource(Integer publicResourceId, String userName) {
        boolean re = false;
        User user = udao.inquireByName(userName);
        PublicResource p = prdao.inquirePublicResourceById(publicResourceId);
        if( p.getUserId() == user.getId() ){
            if(p.getType() == 0){
                Folder folder = fdao.inquireFolderById(p.getUserResourceId());
                folder.setS_flag(false);
                fdao.modifyFolder(folder);
            }else if(p.getType() == 1){
                UserResource userResource = urdao.inquireUserResourceById(p.getUserResourceId());
                userResource.setS_flag(false);
                urdao.modifyResource(userResource);
            }
            if( prdao.removeResourceById(publicResourceId) > 0){
                //处理收藏该资源的用户延迟到用户首次查询时处理
                List<SubResource> subResource = srdao.inquireSubResourceByPId(publicResourceId);
                for (SubResource e : subResource){
                    e.setIsD_flag(true);
                    srdao.modifySubResource(e);
                }
                re = true;
            }
        }

        return re;
    }

    /**
     * @Description:    获取自己创建的共享文件
     * @author Turbine
     * @param
     * @param publicResourceId
     * @param userName
     * @return java.lang.Object
     * @date 2023/2/6 18:29
     */
    public List<PublicResourceDTO> getOwnPublicResource(Integer publicResourceId, String userName) {
        User user = udao.inquireByName(userName);
        List<PublicResourceDTO> re = new ArrayList<>();

        if(publicResourceId == null){
            List<PublicResource> list = prdao.inquireUserPublicResource(user.getId());
            for(PublicResource e : list){
                PublicResourceDTO dto = new PublicResourceDTO();
                dto.assemble(e);
                re.add(dto);
            }
        }else{
            PublicResource resource = prdao.inquirePublicResourceById(publicResourceId);
            if(resource != null){
                PublicResourceDTO dto = new PublicResourceDTO();
                dto.assemble(resource);
                re.add(dto);
            }
        }

        return re;
    }


    /**
     * @Description: 分页模糊查询指定共享资源信息
     * @author Turbine
     * @param
     * @param name   资源名
     * @param size   每页数据数
     * @param page   当前访问页码
     * @return java.util.List<com.turbine.tnd.dto.PublicResourceDTO>
     * @date 2023/2/6 18:53
     */
    public List<PublicResourceDTO> getPublicResource(String name, Integer size, Integer page) {
        List<PublicResourceDTO> list = new ArrayList<>();
        List<PublicResource> resources ;
        if(name != null){
            resources = prdao.inquireLikePublicResource("%"+name+"%",size*(page-1),size);
        }else {
            resources = prdao.inquireAllPublicResource(size*(page-1),size);
        }

        if(resources != null){
            for (PublicResource e : resources){
                PublicResourceDTO dto = new PublicResourceDTO();
                dto.assemble(e);
                list.add(dto);
            }
        }

        return list;
    }

    /**
     * @Description:                    转存共享资源至指定的路径
     * @author Turbine
     * @param
     * @param publicResourceId          公开资源id
     * @param userName
     * @param parentId                  转存至指定父文件夹id
     * @return boolean
     * @date 2023/2/6 21:44
     */
    public boolean savePublicResource(Integer publicResourceId, String userName,Integer parentId) {
        boolean re = false;
        PublicResource publicResource = prdao.inquirePublicResourceById(publicResourceId);
        User user = udao.inquireByName(userName);

        if(publicResource != null){
            switch (publicResource.getType()){
                case 0 :
                    s_ur.saveFolder(publicResource.getUserResourceId(),user.getId(),parentId);
                    break;
                case 1:
                    UserResource userResource = urdao.inquireUserResourceById(publicResource.getUserResourceId());
                    userResource.setOriginalName(publicResource.getName());
                    s_ur.saveFile(userResource,parentId,user.getId());
                    break;
            }
            re = true;
        }
        return re;
    }

    public boolean addSubResource(Integer publicResourceId, String userName) {
        boolean re = false;
        User user = udao.inquireByName(userName);

        SubResource sr = new SubResource(user.getId(),publicResourceId);
        if(srdao.inquireOwnSubResource(publicResourceId,user.getId()) != null )return true;

        if( srdao.addSubResource(sr) > 0 ){
            prdao.incrCollectNum(publicResourceId);
            re = true;
        }

        return re;
    }
    /**
     * @Description: 获取用户订阅资源列表
     * @author Turbine
     * @param
     * @param userName
     * @return java.util.List<com.turbine.tnd.dto.PublicResourceDTO>
     * @date 2023/2/9 20:27
     */
    public List<PublicResourceDTO> getSubResource(String userName) {
        List<PublicResourceDTO> list = new ArrayList<>();
        User user = udao.inquireByName(userName);
        List<SubResource> s_resources = srdao.inquireSubResource(user.getId());
        for (SubResource e : s_resources){
            PublicResource p = prdao.inquirePublicResourceById(e.getPublicResourceId());
            PublicResourceDTO dto = new PublicResourceDTO();
            if(p != null){
                dto.assemble(p);
                dto.setD_flag(e.getIsD_flag());
            }else{
                //原资源已经被删除
                dto.setD_flag(e.getIsD_flag());
                dto.setName("资源已经被主人取消共享");
            }


            list.add(dto);
        }

        return list;
    }

    /**
     * @Description:    移除指定用户关注的资源
     * @author Turbine
     * @param
     * @param id        subResourceId
     * @return boolean
     * @date 2023/2/8 16:15
     */
    public boolean delSubResource(Integer id) {
        return srdao.removeSubResource(id) > 0 ;
    }


}
