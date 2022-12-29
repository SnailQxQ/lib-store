package com.turbine.tnd.bean;

import lombok.Data;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/26 20:17
 */
@Data
public class TempFile {
    long time;
    String fileId;
    String userName;


    public TempFile( String fileId, String userName,long time) {
        this.time = time;
        this.fileId = fileId;
        this.userName = userName;
    }

    public TempFile(){}

    public TempFile(long nanoTime) {
        this.time = nanoTime;
    }
}
