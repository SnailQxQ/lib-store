package com.turbine.tnd.dto;

import lombok.Data;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/12 14:56
 */
@Data
public class FileDownLoadDTO {
    private int chunkNum;
    private byte[] data;
}
