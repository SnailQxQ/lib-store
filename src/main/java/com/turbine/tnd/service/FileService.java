package com.turbine.tnd.service;

import com.turbine.tnd.bean.Message;
import com.turbine.tnd.bean.ResultCode;
import com.turbine.tnd.bean.TempFile;
import com.turbine.tnd.dao.TempFileDao;
import com.turbine.tnd.dto.FileRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/26 20:04
 */
@Service
public class FileService {
    @Autowired
    @Qualifier("EruptUploadStrategy")
    SliceFileService supload;

    @Autowired
    TempFileDao tfdao;

    @Value("${file.upload.tempOverDueTime}")
    long overdueTime;



   /* public Message undoSlicUpload(FileRequestDTO frdto) {
        Message message = new Message(ResultCode.SUCCESS);
        supload.deleteTempFile(frdto);
        return message;
    }*/

    //移除过期时间的临时文件
    public void removeOverDueTempFile() {
        List<TempFile> tempFiles = tfdao.inquireLastTempFile(new TempFile(System.nanoTime()-overdueTime));

        if(tempFiles != null){
            for (TempFile tf : tempFiles){
                System.out.println(tf.getFileId());

                FileRequestDTO frdto = new FileRequestDTO();
                frdto.setFileName(tf.getFileId());
                frdto.setUserName(tf.getUserName());
                supload.deleteTempFile(frdto);
            }
        }
    }
}
