package com.turbine.tnd.utils;

/**
 * @author 邱信强
 * @Description
 * @date 2022/1/23 20:08
 */
public interface Factory<T> {
    //获取资源
    T getResource();
}
