package com.turbine.tnd.utils;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/23 18:54
 */
//过滤器代理工厂，使用双重校验锁
@Component
public class FilterFactor {

   private static volatile Filter filter;

   public enum filterOpt {
       AC_FILTER("com.turbine.tnd.utils.StringACFilter")
       ,GEN_FILTER("com.turbine.tnd.utils.StringFilter");

       String clazzName;
        filterOpt(String stringFilter) {
            clazzName = stringFilter;
       }
   }


   //DLC
    public Filter getResource(filterOpt option) {
        if(filter == null){
            synchronized (FilterFactor.class){
                //使用动态代理类反射创建对象
                if(filter == null){
                    try {
                        Class<?> clazz = Class.forName(option.clazzName);
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        filter = (Filter) constructor.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return filter;
    }

}
