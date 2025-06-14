package com.skymmer.mapper;


import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.Travel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.cglib.core.Local;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TravelMapper {

    @Options(keyProperty = "id", useGeneratedKeys = true)
    int insert(Travel travel);

    List<Travel> select(@Param("start") Timestamp start,
                        @Param("end") Timestamp end,
                        @Param("location") String location);

    List<GpsInfo> genTra(@Param("start") Timestamp start,
                         @Param("end") Timestamp end);
    @Select("select * from TB_TRAVEL")
    List<Travel> selectALl();

    @Select("select IMAGE_PATH as imagePath from TB_TRAVEL")
    List<String> selectPath();
}

