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
            response.sendRedirect("/error/tokenError");
        }

        return flag;
    }
}
