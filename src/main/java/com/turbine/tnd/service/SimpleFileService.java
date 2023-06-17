package com.turbine.tnd.service;

import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.bean.ResourceType;
import com.turbine.tnd.bean.UserResource;
import com.turbine.tnd.config.UploadFileConfig;
import com.turbine.tnd.dao.ResourceDao;
import com.turbine.tnd.dao.UserResourceDao;
import com.turbine.tnd.utils.CommandUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/25 10:35
 */
@Service("SimpleFileService")
@ConfigurationProperties("file.upload")
@Data
@Slf4j
public class SimpleFileService{

    //根目录 /static
    @Value("${file.upload.fileFolder}")
    String fileFolder ;
    @Value("${file.upload.baseDir}")
    String baseDir;

    @Autowired
    ResourceDao redao;
    @Autowired
    UserResourceDao urdao;

    //简单文件上传

    /**
     *
     * @param multipartFile
     * @param resourceName   源文件名
     * @param userId        上传的用户id
     * @param typeId        文件类型id
     * @param suffix        文件后缀
     * @return
     */
    @Transactional
    public  boolean upload(MultipartFile multipartFile, String resourceName, int userId,int typeId,String suffix,int parentId) {
        boolean flag = true;
        LocalDateTime date = LocalDateTime.now();
        try {
            String name = FileUtils.getFileMD5(multipartFile.getInputStream());
            if( "".equals(name) )return false;
            //秒传支持
            if(!hasExist(name)){
                //System.out.println("文件MD5："+name);
                //创建按照这个目录创建，传输的时候会自带上设置的前缀 先用临时文件创建来解决
                String dir = File.separator+date.getYear()+File.separator+date.getMonth()+File.separator+date.getDayOfMonth();

                String path = fileFolder+dir+File.separator+name.toString()+ File.separator+name.toString()+suffix;
                String parent = baseDir+fileFolder+dir+File.separator+name.toString();

                File target = new File(parent,name.toString()+suffix);

                //log.debug("========================target path: "+target.getAbsolutePath());

                if(!target.exists()){
                    target.mkdirs();
                    target.createNewFile();
                }


                multipartFile.transferTo(target);

                Resource re = new Resource();
                re.setType_id(typeId);
                re.setLocation(path);
                re.setSize(multipartFile.getSize());
                re.setFileName(name);

                redao.addResource(re);
                urdao.addUserResource(new UserResource(userId,re.getId(),name,resourceName,parentId,typeId));
                redao.addReourceType(name,typeId);
                //若为视频文件则对应进行视频处理
                if( ".mp4".equals(suffix) ){
                    //文件第一次传输要时间，传输完成后才进行分片
                    new Thread(()->{
                        while(!target.exists()){
                            try {
                                System.out.println("文件"+target.getName()+ " 位置："+target.getAbsolutePath()+ " 还在生成中....成功后进行分配操作");
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        CommandUtils.processVideo(baseDir+path,parent+File.separator+name.toString()+".m3u8");
                    }).start();
                }
            }else {
                Resource resource = redao.inquireByName(name);
                urdao.addUserResource(new UserResource(userId,resource.getId(),name,resourceName,parentId,typeId));
                log.debug("文件已经存在 MD5: "+name);
            }

        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        }

        return  flag;
    }

    //查询文件是否存在
    public boolean hasExist(String name) {
        Resource resource = redao.inquireByName(name);
        return resource != null;
    }

    //查询文件类型
    public ResourceType inquireType(String suffix) {
        return redao.inquireType(suffix);
    }
}
