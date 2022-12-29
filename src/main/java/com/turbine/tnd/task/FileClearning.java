package com.turbine.tnd.task;

import com.turbine.tnd.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/26 21:27
 */

@Component
@Slf4j
@EnableScheduling
public class FileClearning {
    @Autowired
    FileService fs;

    @Scheduled(cron = "0 0 23 * * ?")
    public void clearOverDueTempFileTask(){
        log.debug("======执行临时文件清除======");
        fs.removeOverDueTempFile();
    }
}
