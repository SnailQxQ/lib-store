package com.turbine.tnd.utils;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/23 18:54
 */
//过滤器代理工厂，使用双重校验锁
@Resource
public class FilterFactor implements Factory<Filter>{

   private static volatile StringFilter sfilter;


   //DLC
    @Override
    public Filter getResource() {
        if(sfilter == null){
            synchronized (FilterFactor.class){
                //使用动态代理类反射创建对象
                if(sfilter == null){
                    try {
                        Class<?> clazz = Class.forName(StringFilter.class.getName());
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        sfilter = (StringFilter) constructor.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return sfilter;
    }

}
