package com.turbine.tnd.intercepter;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.service.UserService;
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

    //获取文件前验证文件是否存在，创建文件前验证父文件是否存在

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        boolean flag = true;

        if(requestURI.contains("/user/resource")){
            Cookie[] cookies = request.getCookies();
            String userName = null;
            if(cookies != null){
                for(Cookie cookie : cookies){
                    if( "userName".equals( cookie.getName() ) )userName = cookie.getValue();
                }
            }

            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String parentId = null;
            //先验证文件夹是否存在
            if( (parentId = (String)pathVariables.get("parentId") ) != null &&  !"0".equals(parentId) ){

                if( !us.FolderIsExist(Integer.parseInt(parentId)) ){
                    flag = false;
                    response.sendRedirect("/error/resourceNotFound");
                }

            }else if(requestURI.contains("/user/resource/file") ){
                //获取路径变量
                String fileName = (String)pathVariables.get("fileId");
                String resourceId = (String)pathVariables.get("resourceId");

                if(fileName != null && parentId != null){
                    Resource resource = us.inquireResource(fileName);
                    if(!us.ResourceIsExist(userName,fileName,Integer.parseInt(parentId)) || resource == null){
                        flag = false;
                        response.sendRedirect("/error/resourceNotFound");
                    }
                }
                if(resourceId != null){
                    if(!us.ResourceIsExist(Integer.parseInt(resourceId),userName)){
                        flag = false;
                        response.sendRedirect("/error/resourceNotFound");
                    }
                }

            }else if(requestURI.contains("/user/resource/folder")){
                String  folderId = (String)pathVariables.get("folderId");
                if(folderId != null){
                    if(Integer.parseInt(folderId) != 0 &&  !us.FolderIsExist(Integer.parseInt(folderId)) ){
                        flag = false;
                        response.sendRedirect("/error/resourceNotFound");
                    }
                }

            }
        }

        return flag;
    }
}
