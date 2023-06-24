package com.turbine.tnd.intercepter;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.service.UserResourceService;
import com.turbine.tnd.service.UserService;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Turbine
 * @Description 判断用户是否有该资源
 * @date 2022/2/9 22:17
 */
@Component
public class fileInterceptor implements HandlerInterceptor {
    //拦截器在bean 注入之前，使用webmvcauto..让其提前初始化
    @Autowired
    UserService us;
    @Autowired
    UserResourceService s_ur;

    //获取文件前验证文件是否存在，创建文件前验证父文件是否存在

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        boolean flag = true;
        String f_url = null;

        if( requestURI.contains("/resource/socket"))return true;

        if( (requestURI.contains("/user/resource/share") && !requestURI.contains("transfer") )
                || requestURI.contains("/user/resource/shared")
                || requestURI.contains("/user/resource/file/expire") )return true;

        if(requestURI.contains("/user/resource")){
            Cookie[] cookies = request.getCookies();
            String userName = null;
            if(cookies != null){
                for(Cookie cookie : cookies){
                    if( "userName".equals( cookie.getName() ) )userName = cookie.getValue();
                }
            }

            User user = us.getUser(userName);

            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String parentId = null;
            //先验证文件夹是否存在
            if(pathVariables != null &&  (parentId = (String)pathVariables.get("parentId") ) != null &&  !"0".equals(parentId) ){

                if( !s_ur.FolderIsExist(Integer.parseInt(parentId)) ){
                    flag = false;
                    f_url = "/error/resourceNotFound";
                }

            }else if(requestURI.contains("/user/resource/file") ){
                //用户资源id
                String resource_id = (String) pathVariables.get("resourceId");
                String type = (String) pathVariables.get("type");
                if(type == null && resource_id != null){
                    Integer resourceId = Integer.parseInt(resource_id);

                    if(resourceId != null && !s_ur.hasResource(resourceId,user.getId())){
                        flag = false;
                        f_url = "/error/resourceNotFound";
                    }
                }

            }else if(requestURI.contains("/user/resource/folder")){
                String  folderId = (String)pathVariables.get("folderId");
                if(folderId != null){
                    if(Integer.parseInt(folderId) != 0 &&  !s_ur.hasFolder(Integer.parseInt(folderId),user.getId()) ){
                        flag = false;
                        f_url = "/error/resourceNotFound";
                    }
                }

            }
        }

        if( !flag ) request.getRequestDispatcher(f_url).forward(request,response);

        return flag;
    }
}
