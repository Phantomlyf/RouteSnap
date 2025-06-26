package com.skymmer.mapper;


import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.ListInfo;
import com.skymmer.pojo.Travel;
import org.apache.ibatis.annotations.*;
import org.springframework.cglib.core.Local;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TravelMapper {

    @Options(keyProperty = "id", useGeneratedKeys = true)
    int insert(Travel travel);

    List<Integer> selectIds();

    List<Integer> selectByCase(@Param("start") Timestamp start,
                        @Param("end") Timestamp end,
                        @Param("location") String location);

    Travel selectById(@Param("id") Integer id);

    List<GpsInfo> selectGpsInfos();

    List<ListInfo> selectShortInfos();

    List<Integer> genTra(@Param("start") Timestamp start,
                         @Param("end") Timestamp end);

    void updateLonLat(@Param("id") Integer id,
                      @Param("lat") Double lat,
                      @Param("lon") Double lon,
                      @Param("gcjLat") Double gcjLat,
                      @Param("gcjLon") Double gcjLon);

    void updateContent(@Param("id") Integer id,
                       @Param("content") String content);

    @Delete("delete from TB_TRAVEL where  ID = #{id}")
    void deleteById(@Param("id")Integer id);

    @Select("select * from TB_TRAVEL")
    List<Travel> selectALl();

    @Select("select IMAGE_PATH as imagePath from TB_TRAVEL")
    List<String> selectPath();


}

