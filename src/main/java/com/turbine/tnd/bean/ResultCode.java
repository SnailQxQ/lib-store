package com.turbine.tnd.bean;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/25 11:34
 */
public enum ResultCode {
     SUCCESS(200,"success!")
    ,ERROR_400(400,"客户端请求存在语法错误!")
    ,ERROR_401(401,"请先登入再访问！")
    ,ERROR_403(403,"您没有相应的权限，请先申请权限！")
    ,ERROR_404(404,"资源未找到！")
    ,ERROR_500(500,"发生错误，请稍后再试!")
    ,ERROR_503(503,"服务繁忙，请稍后再试!");


    private int code;
    private String message;


    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
