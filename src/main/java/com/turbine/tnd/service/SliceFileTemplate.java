package com.turbine.tnd.service;

import com.turbine.tnd.bean.*;
import com.turbine.tnd.dao.ResourceDao;
import com.turbine.tnd.dao.TempFileDao;
import com.turbine.tnd.dao.UserDao;
import com.turbine.tnd.dao.UserResourceDao;
import com.turbine.tnd.dto.FileDownLoadDTO;
import com.turbine.tnd.dto.FileUploadDTO;
import com.turbine.tnd.dto.FileRequestDTO;
import com.turbine.tnd.dto.ResourceDTO;
import com.turbine.tnd.utils.CommandUtils;
import com.turbine.tnd.utils.Filter;
import com.turbine.tnd.utils.FilterFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Turbine
 * @Description 模板方法设计模式，定义出分片上传的整题骨架
 * @date 2022/1/26 19:36
 */

@Slf4j
@Component
@SuppressWarnings("all")
public abstract class SliceFileTemplate implements SliceFileService {
    @Autowired
    ResourceDao rdao;
    @Autowired
    FilterFactor filterFactor;
    @Value("${file.upload.fileFolder}")
    String fileFolder;
    @Value("${file.upload.baseDir}")
    String baseDir;
    @Value("${file.upload.tmpDir}")
    String tempDir;
    @Autowired
    UserDao udao;
    //上传的路径，创建临时文件到时候创建
    String uploadDirPath ;
    @Autowired
    TempFileDao tfdao;
    @Autowired
    UserResourceDao urdao;
    //成功后的资源id
    Integer userResourceId;
    //丢给子类去实现具体的上传方法
    public abstract boolean upload(FileRequestDTO param);
    //整合方法
    protected abstract String mergeFile(FileRequestDTO param, String uploadDirPath);
    //模板上传的核心
    @Override
    public  FileUploadDTO sliceUpload(FileRequestDTO param) {
        boolean isOk = this.upload(param);
        FileUploadDTO fudto = new FileUploadDTO();
        fudto.setChunkNum(param.getChunkNum());

        //上传完毕后要记录对应conf 文件中对应的块是否成功
        if(isOk){

            fudto.setAccomplish(true);
            boolean b = checkAndSetUploadProgress(param, uploadDirPath);
            if( b ){
                User user = udao.inquireByName(param.getUserName());
                UserResource ur = urdao.inquireUserResourceById(userResourceId);
                ResourceDTO dto = new ResourceDTO(ur);
                dto.setFileSize(param.getTotalSize());
                dto.setFileType(param.getOriginalName().substring(param.getOriginalName().lastIndexOf(".")));
                fudto.setAllSuccess(b);

                fudto.setResource(dto);
            }

        }else{
            fudto.setAccomplish(false);
        }

        return fudto;
    }

    @Override
    public FileDownLoadDTO sliceDownload(FileRequestDTO param) {
        FileDownLoadDTO result = new FileDownLoadDTO();
        result.setChunkNum(param.getChunkNum());

        Resource resource = rdao.inquireByName(param.getFileName());
        String location = resource.getLocation();
        File file = new File(location);
        if(file.exists()){

            byte[] data = null;

            try ( RandomAccessFile accessFile = new RandomAccessFile(file,"r") ;){

                int start = (param.getChunkNum()-1) * param.getChunkSize();
                if(accessFile.length()-start < param.getChunkSize())data = new byte[(int)accessFile.length()-start];
                else  data = new byte[param.getChunkSize()];

                if(start < 0 || start>accessFile.length()){
                    log.debug("请求资源： "+param.getFileName()+" 第 "+param.getChunkNum() +" 块 index出错！ fileSize:"+accessFile.length());
                    return result;
                }

                accessFile.seek(start);
                accessFile.read(data);

                result.setData(data);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return result;
    }


    /**
     * 检查还有已经传输完成的块
     */
    public List<Integer> checkFinished(FileRequestDTO param) {
        String uploadDirPath = FileUtils.getPath(tempDir);
        File configFile = new File(getTempCofPath(param.getFileName(),param.getUserName()));
        List<Integer> result = new ArrayList<>();

        try ( RandomAccessFile accessFile = new RandomAccessFile(configFile, "r") ){
            if(configFile.exists()){
                byte[] bytes = FileUtils.readFileToByteArry(accessFile);
                int i=1;

                for(byte b : bytes){
                    if(b == Byte.MAX_VALUE)result.add(i);
                    i++;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 不同用户上传同一个文件生成的临时文件要不同
     * @param fileId    文件id
     * @param userName    用户名
     * @return
     */
    public String getTempCofPath(String fileId,String userName){
        //baseDir 有注入的属性先后问题，这个放到后面生成
        if(this.uploadDirPath == null)this.uploadDirPath = FileUtils.getPath(tempDir);
        return baseDir+"/"+uploadDirPath+"/"+fileId+ userName +".conf";
    }

    /**
     *
     * @param fileId    文件hashId
     * @param userName  用户名
     * @param chunkNum  当前文件块号
     * @return
     */
    public String getTempPath(String fileId,String userName,int chunkNum){
        if(this.uploadDirPath == null)this.uploadDirPath = FileUtils.getPath(tempDir);
        return baseDir+File.separator+uploadDirPath + File.separator +fileId+userName+ "&" +chunkNum+".tmp";
    }

    /**
     * 检查并设置文件上传进度
     * 原临时文件生成的地方再生成一个.conf文件来记录
     * 文件全部写入完成就会返回true
     * @param param
     * @param uploadDirPath
     */
    public boolean checkAndSetUploadProgress(FileRequestDTO param, String uploadDirPath) {

        File configFile = new File(getTempCofPath(param.getFileName(),param.getUserName()));
        byte isComplete = 0;
        //new RandomAccessFile(configFile,"rw") 会自动帮忙创建文件
        RandomAccessFile accessFile = null;
        try {
            if(!configFile.exists()){
                configFile.createNewFile();
                TempFile tempFile = new TempFile(param.getFileName(),param.getUserName(),System.nanoTime());
                tfdao.addTempFile(tempFile);
            }
            accessFile  = new RandomAccessFile(configFile,"rw");

            accessFile.setLength(param.getTotalChunkNum());
            //设置写入完成标记的位置,在完成的地方直接写入完成标记
            accessFile.seek(param.getChunkNum()-1);
            accessFile.write(Byte.MAX_VALUE);
            //全为 127 那就是全写完了
            byte[] lists = FileUtils.readFileToByteArry(accessFile);
            if(lists != null){
                isComplete = Byte.MAX_VALUE;
                for(int i=0 ;i<lists.length && isComplete !=0 ;i++){
                    isComplete &= lists[i];
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(accessFile != null)accessFile.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //分片全部传输完成后进行文件的信息保存
        boolean isOk = SaveFile(param,configFile,uploadDirPath,isComplete);

        return isOk;
    }

    //保存文件信息数据到数据库,完成的话就将原文件进改名并存储信息到数据库
    @Transactional
    protected boolean SaveFile(FileRequestDTO param, File configFile, String uploadDirPath, byte isComplete) {
        if(isComplete == Byte.MAX_VALUE){
            //进行文件整合
            String location = null;
            if(( location = this.mergeFile(param, uploadDirPath) ) != null){
                configFile.delete();
                tfdao.removeTempFile(new TempFile(param.getFileName(),param.getUserName(),System.nanoTime()));
                Resource re = new Resource();
                Filter<String> filter = filterFactor.getResource(FilterFactor.filterOpt.AC_FILTER);

                int idx = param.getOriginalName().lastIndexOf(".");
                String suffix = param.getOriginalName().substring(idx);
                ResourceType type = rdao.inquireType(suffix);

                re.setFileName(param.getFileName());
                re.setSize(param.getTotalSize());
                re.setLocation(location);
                re.setType_id(type.getId());

                if(rdao.addResource(re) > 0 ){

                    String originalName =filter.filtration(param.getOriginalName().substring(0,idx));
                    User user = udao.inquireByName(param.getUserName());
                    UserResource ur = new UserResource(user.getId(),re.getId(), param.getFileName(), originalName + suffix, param.getParentId(), type.getId());
                    urdao.addUserResource(ur);
                    userResourceId = ur.getId();
                    rdao.addReourceType(param.getFileName(),type.getId());

                }
                int i = location.lastIndexOf('.');
                String target = location.substring(0,i)+File.separator+param.getFileName()+"_out.m3u8";
                String path = location;
                if( ".mp4".equals(suffix) ){
                    new Thread(()->{
                        CommandUtils.processVideo(path,target);
                    }).start();
                }

            }

            return location != null;
        }

        return false;
    }

    /**
     *
     * @param file          待修改的文件
     * @param toFileName    传入唯一个uuid名
     */
    public boolean renameFile(File file,String toFileName){
        if( !file.exists() || file.isDirectory() ){
            log.debug("File don't exists!");
            return false;
        }
        File nFile = new File(file.getParent()+toFileName);
        boolean b = file.renameTo(nFile);

        return b;
    }


    //创建出来临时文件的位置 根据文件hash 和块号创建对应的文件
    //临时文件目录 static/TEMP/...
    protected File createTmpFile(FileRequestDTO param)  {
        String filePath = getTempPath(param.getFileName(),param.getUserName(),param.getChunkNum());
        //只要创建了目录就行，这个transfor 会自动帮创建
        File temp = null;
        try {

            //创建目录
            File dir = new File (baseDir+"/"+uploadDirPath);

            if(!dir.exists()){
                dir.mkdirs();
                dir.createNewFile();
            }
            temp = new File(filePath);

            if(!temp.exists())
                temp.mkdirs();
                temp.createNewFile();

        }catch (Exception e) {
            log.debug("==============createTmpFile ： 临时文件目录创建异常==============");
            e.printStackTrace();
        }

        return temp;
    }

    /**
     * 根据文件名判断是否是支持的类型
     * @param fileName
     * @return
     */
    public boolean isSupport(String fileName) {
        boolean flag = false;
        if(fileName != null){
            int idx = fileName.lastIndexOf(".");
            String suffix = fileName.substring(idx);
            ResourceType type = rdao.inquireType(suffix);
            flag = type != null;
        }
        return flag;
    }


}
