package com.skymmer.controller;

import com.skymmer.mapper.TravelMapper;
import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.ListInfo;
import com.skymmer.pojo.Travel;
import com.skymmer.service.TravelService;
import com.skymmer.utils.DateConverter;
import com.skymmer.utils.ImageConverter;
import com.skymmer.utils.MetaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.skymmer.pojo.Result;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
        int id = 0;
        try {
            id = travelService.upload(file,location,content);
            if(id>0){
                return Result.success(id);
            }
            else{
                return  Result.error(1101,"游记上传失败");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/upload2")
    public Result uploadTravel(@RequestParam String previewPath, @RequestBody Travel travel ){
        String decodedPath = URLDecoder.decode(previewPath, StandardCharsets.UTF_8);
        System.out.println(decodedPath);
        int id = 0;
        try {
            id = travelService.upload(decodedPath,travel);
            if(id>0){
                return Result.success(id);
            }
            else{
                return  Result.error(1101,"游记上传失败");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    @GetMapping("/listGps")
    public Result listGps(){
        List<GpsInfo> gpsList = travelService.listGps();
        if(gpsList != null){
            return  Result.success(gpsList);
        }
        else{
            return Result.error(1104,"所有游记gps信息获取失败");
        }

    }

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page){
        List<ListInfo> shortList = travelService.listShort(page);
        if(shortList != null){
            return Result.success(shortList);
        }
        else{
            return Result.error(1105,page+"页简略数据获取失败");
        }
    }



    @GetMapping("/listBycase")
    public Result selectTravel(@RequestParam(required=false) String startTime,
                               @RequestParam(required=false) String endTime,
                               @RequestParam(required=false) String location){
        LocalDateTime start = DateConverter.convertToLocalDateTime(startTime);
        LocalDateTime end = DateConverter.convertToLocalDateTime(endTime);
        List<Integer> travels = travelService.selectTravel(start,end,location);
        return Result.success(travels);
    }

    @PutMapping("/change/{id}")
    public Result changeLonLat(@PathVariable Integer id,
                               @RequestParam Double lat,
                               @RequestParam Double lon,
                               @RequestParam String content){
        try {
            if(lat != null && lon != null){
                travelService.updateLonLat(id,lat,lon);
            }
            if(content != null){
                travelService.updateContent(id,content);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error(1106,"游记修改失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteTravel(@PathVariable Integer id){
        try {
            travelService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
           return Result.error(1107,"游记删除失败");
        }
    }



}
