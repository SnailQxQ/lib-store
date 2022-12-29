package com.turbine.tnd.dao;

import com.turbine.tnd.bean.TempFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Turbine
 * @Description
 * @date 2022/2/26 20:18
 */
@Mapper
public interface TempFileDao {
    List<TempFile> inquireLastTempFile(TempFile tempFile);

    int addTempFile(TempFile tempFile);

    int removeTempFile(TempFile tempFile);
}
