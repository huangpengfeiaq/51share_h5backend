package com.scriptures.shareApp.dao.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.scriptures.shareApp.dao.entity.Memberlabel;

public interface MemberlabelMapper {
    int deleteByPrimaryKey(String id);

    int insert(Memberlabel record);

    int insertSelective(Memberlabel record);

    Memberlabel selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Memberlabel record);

    int updateByPrimaryKey(Memberlabel record);
    
    /////////////////////////////////////////////////
    
    Memberlabel selectById(@Param("id")String id);
    
    List<Memberlabel> selectAll();
    
    Memberlabel checkName(@Param("labelName")String labelName);
  
}