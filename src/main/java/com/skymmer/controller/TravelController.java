package com.skymmer.controller;

import com.skymmer.mapper.TravelMapper;
import com.skymmer.pojo.Travel;
import com.skymmer.service.TravelService;
import com.skymmer.utils.DateConverter;
import com.skymmer.utils.MetaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.skymmer.pojo.Result;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/travel")
public class TravelController {

    @Autowired
    TravelService travelService;
    @PostMapping("/upload")
    public Result uploadTravel(@RequestParam MultipartFile file,@RequestParam String location, @RequestParam String content){
        int id = travelService.upload(file,location,content);
        if(id>0){
            return Result.success(id);
        }
        else{
            return  Result.error(1101,"游记上传失败");
        }


    }
    @GetMapping("/listId")
    public Result listId(){
        List<Integer> ids = travelService.listId();
        if(ids != null){
            return Result.success(ids);
        }
        else{
            return Result.error(1102,"所有游记id获取失败");
        }
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable("id") Integer id){
        Travel travel= travelService.getById(id);
        if(travel != null){
            return Result.success(travel);
        }
        else{
            return Result.error(1103,"游记id信息不正确或数据库损坏");
        }
    }

    @GetMapping("listBycase")
    public Result selectTravel(@RequestParam(required=false) String startTime,
                               @RequestParam(required=false) String endTime,
                               @RequestParam(required=false) String location){
        LocalDateTime start = DateConverter.convertToLocalDateTime(startTime);
        LocalDateTime end = DateConverter.convertToLocalDateTime(endTime);
        List<Travel> travels = travelService.selectTravel(start,end,location);
        return Result.success(travels);
    }


}
