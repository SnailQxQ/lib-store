package com.turbine.tnd.service;

import com.turbine.tnd.dto.FileUploadRequestDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Turbine
 * @Description 多文件并发接收并整合
 * @date 2022/1/26 19:35
 * //TODO:NIO 传输数据到一般系统崩了会怎么样
 */
@Slf4j
@Service("EruptUploadStrategy")
public class EruptUploadStrategy extends SliceUploadTemplate{
    @Override
    public boolean upload(FileUploadRequestDTO param) {
        boolean flag = true;
        MultipartFile file = param.getFile();
        File tempFile = super.createTmpFile(param);
        try {
            file.transferTo(tempFile);
            System.out.println("设置段："+param.getChuckNum()+"完成！");
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        }

        return flag;
    }

    /**
     * 将所有的临时文件整合 使用NIO
     * @param param
     * @param uploadPath    上传路径
     * @return
     */
    @Override
    protected boolean mergeFile(FileUploadRequestDTO param,String uploadPath) {
        log.debug("开始整合文件 ："+param.getFileName());
        boolean flag = true;
        int n = param.getTotalChuckNum();
        int idx = param.getFile().getOriginalFilename().lastIndexOf(".");
        String suffix = param.getFile().getOriginalFilename().substring(idx);
        String dir = FileUtils.getPath(this.baseDir);
        //写入到 static/。。。。
        String result_path = dir+"/"+param.getFileName()+suffix;

        File result = new File(result_path);
        FileChannel channel = null;
        try {
            File dirF = new File(dir);

            if(!dirF.exists()){
                dirF.mkdirs();
            }

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //追加模式
            channel = new FileOutputStream(result,true).getChannel();
            int start = 0;
            for(int i=1 ;i<= n ;i++){
                String path = this.baseDir+"/"+uploadPath+"/"+param.getFileName()+"&"+i+".tmp";
                File temp = new File(path);
                FileChannel tmpChannel = new FileInputStream(temp).getChannel();

                int len;
                while((len = tmpChannel.read(buffer)) != -1){
                    buffer.flip();//反转指针
                    channel.write(buffer,start);
                    start += len;
                    buffer.clear();
                }
                tmpChannel.close();
                buffer.clear();
            }
            //批量清除临时文件
            for(int i=1 ;i<= n ;i++){
                String path = this.baseDir+"/"+uploadPath+"/"+param.getFileName()+"&"+i+".tmp";
                File temp = new File(path);
                temp.delete();
            }


        }  catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // boolean success = renameFile(tmpFile, param.getFileName());

        return flag;
    }
}
