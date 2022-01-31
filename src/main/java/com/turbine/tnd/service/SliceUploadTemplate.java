package com.turbine.tnd.service;

import com.turbine.tnd.bean.Resource;
import com.turbine.tnd.bean.ResourceType;
import com.turbine.tnd.bean.User;
import com.turbine.tnd.dao.ResourceDao;
import com.turbine.tnd.dao.UserDao;
import com.turbine.tnd.dto.FileUploadDTO;
import com.turbine.tnd.dto.FileUploadRequestDTO;
import com.turbine.tnd.utils.Filter;
import com.turbine.tnd.utils.FilterFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * @author Turbine
 * @Description 模板方法设计模式，定义出分片上传的整题骨架
 * @date 2022/1/26 19:36
 */

@Slf4j
@Component
public abstract class SliceUploadTemplate implements SliceUploadStrategy{
    @Autowired
    ResourceDao rdao;
    @Autowired
    FilterFactor filterFactor;
    @Value("${file.upload.baseDir}")
    String baseDir;
    @Value("${file.upload.tmpDir}")
    String tempDir;
    @Autowired
    UserDao udao;
    //上传的路径，子类实现上传后填充
    String uploadDirPath ;
    //丢给子类去实现具体的上传方法
    public abstract boolean upload(FileUploadRequestDTO param);
    //整合方法
    protected abstract boolean mergeFile(FileUploadRequestDTO param,String uploadDirPath);
    //模板上传的核心
    @Override
    public  FileUploadDTO sliceUpload(FileUploadRequestDTO param) {
        boolean isOk = this.upload(param);
        FileUploadDTO fudto = new FileUploadDTO();
        fudto.setChunkNum(param.getChuckNum());

        //上传完毕后要记录对应conf 文件中对应的块是否成功
        if(isOk){

            fudto.setAccomplish(true);
            boolean b = checkAndSetUploadProgress(param, uploadDirPath);
            fudto.setAllSuccess(b);
        }else{
            fudto.setAccomplish(false);
        }

        return fudto;
    }



    /**
     * 检查还有哪些块没有传输完成
     * @param param
     * @return
     *
     */
    //TODO:检查还有哪些块没有传输完成
    private List<Integer> checkUnFinished(FileUploadRequestDTO param) {
        return null;
    }

    /**
     * 检查并设置文件上传进度
     * 原临时文件生成的地方再生成一个.conf文件来记录
     * 文件全部写入完成就会返回true
     * @param param
     * @param uploadDirPath
     */
    public boolean checkAndSetUploadProgress(FileUploadRequestDTO param, String uploadDirPath) {

        File configFile = new File(baseDir+"/"+uploadDirPath,param.getFileName()+".conf");
        byte isComplete = 0;
        RandomAccessFile accessFile = null;
        try {
            if(!configFile.exists()){
                configFile.createNewFile();
            }
            accessFile = new RandomAccessFile(configFile,"rw");


            accessFile.setLength(param.getTotalChuckNum());
            //设置写入完成标记的位置,在完成的地方直接写入完成标记
            accessFile.seek(param.getChuckNum()-1);
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
            try {
                accessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("随机访问流关闭失败！");
            }
        }
        //分片全部传输完成后进行文件的信息保存
        boolean isOk = SaveFile(param,configFile,uploadDirPath,isComplete);

        return isOk;
    }

    //保存文件信息数据到数据库,完成的话就将原文件进改名并存储信息到数据库
    @Transactional
    protected boolean SaveFile(FileUploadRequestDTO param, File configFile, String uploadDirPath, byte isComplete) {
        if(isComplete == Byte.MAX_VALUE){
            //进行文件整合
            this.mergeFile(param,uploadDirPath);
            configFile.delete();

            Resource re = new Resource();
            Filter<String> filter = filterFactor.getResource(FilterFactor.filterOpt.AC_FILTER);

            int idx = param.getFile().getOriginalFilename().lastIndexOf(".");
            String suffix = param.getFile().getOriginalFilename().substring(idx);
            ResourceType type = rdao.inquireType(suffix);

            re.setFileName(param.getFileName());
            re.setSize(param.getTotalSize());
            re.setLocation(uploadDirPath+"/"+param.getFileName()+suffix);
            re.setType_id(type.getId());

            rdao.addResource(re);
            String originalName =filter.filtration(param.getFile().getOriginalFilename());
            User user = udao.inquireByName(param.getUserName());
            rdao.addResourceUser(user.getId(),param.getFileName(), originalName);
            rdao.addReourceType(param.getFileName(),type.getId());

            return true;
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
            System.out.println("");
            log.debug("File don't exists!");
            return false;
        }
        File nFile = new File(file.getParent()+toFileName);
        boolean b = file.renameTo(nFile);

        return b;
    }


    //创建出来临时文件的位置 根据文件hash 和块号创建对应的文件
    //临时文件目录 static/TEMP/...
    protected File createTmpFile(FileUploadRequestDTO param)  {
        if(this.uploadDirPath == null)this.uploadDirPath = FileUtils.getPath(tempDir);
        String filePath = baseDir+"/"+uploadDirPath + "/" +param.getFileName()+ "&" +param.getChuckNum()+".tmp";
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
