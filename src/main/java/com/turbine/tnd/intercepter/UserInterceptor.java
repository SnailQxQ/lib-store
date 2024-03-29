package com.turbine.tnd.intercepter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/9 14:12
 */
@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = null;
        String userName = null;
        boolean flag = false;
        String url = request.getRequestURI();
        //TODO:临时测试
        if( url.contains("/resource/socket"))return true;


        if( (url.contains("/user/resource/share") && !url.contains("transfer") )
            || url.contains("/user/register")
            || url.contains("/user/resource/file/expire"))return true;

        if( "user".equals(url.substring(1,url.lastIndexOf('/'))) )return true;

        if(cookies != null){
            for(Cookie cookie : cookies){
                if( "token".equals( cookie.getName() ) )token = cookie.getValue();
                if( "userName".equals( cookie.getName() ) )userName = cookie.getValue();
            }
            log.debug("用户身份过期验证：token ："+token +" userName :"+userName);

            if(token != null && userName != null){
                String s_token = null;
                if( (s_token  = (String)request.getSession().getAttribute(userName) ) != null){
                    flag = token.equals(s_token);
                }

            }
        }
        if(!flag) {
            request.getRequestDispatcher("/error/tokenError").forward(request,response);
        }

        return flag;
    }
}
