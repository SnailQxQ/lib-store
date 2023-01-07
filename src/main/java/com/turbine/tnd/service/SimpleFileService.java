package com.turbine.tnd.service;

import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.bean.ResourceType;
import com.turbine.tnd.bean.UserResource;
import com.turbine.tnd.config.UploadFileConfig;
import com.turbine.tnd.dao.ResourceDao;
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
                System.out.println("文件MD5："+name);
                //创建按照这个目录创建，传输的时候会自带上设置的前缀 先用临时文件创建来解决
                String dir = "/"+date.getYear()+"/"+date.getMonth()+"/"+date.getDayOfMonth();

                String path = fileFolder+dir+"/"+name.toString()+suffix;

                //File temp = new File(fileFolder+dir);
                File target = new File(baseDir+fileFolder+dir,name.toString()+suffix);

                //log.debug("========================temp path: "+temp.getAbsolutePath());
                log.debug("========================target path: "+target.getAbsolutePath());

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
                redao.addResourceUser(userId,re.getId(),name,resourceName+suffix,parentId,typeId);
                redao.addReourceType(name,typeId);
            }else {
                Resource resource = redao.inquireByName(name);
                redao.addResourceUser(userId,resource.getId(),name,resourceName+suffix,parentId,typeId);
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
