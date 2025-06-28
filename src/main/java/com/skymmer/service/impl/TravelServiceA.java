package com.skymmer.service.impl;

import com.drew.imaging.ImageProcessingException;
import com.github.pagehelper.PageHelper;
import com.skymmer.mapper.TravelMapper;
import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.ListInfo;
import com.skymmer.pojo.Result;
import com.skymmer.pojo.Travel;
import com.skymmer.service.TravelService;
import com.skymmer.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TravelServiceA implements TravelService {
    @Autowired
    ImageStorageUtils imageStorageUtils;
    @Autowired
    TravelMapper travelMapper;
    @Autowired
    ImageThumbnailGenerator imageThumbnailGenerator;
    @Autowired
    GpsConverter gpsConverter;
    @Autowired
    FileExport fileExport;

    @Value("${page.pageSize}")
    private Integer pageSize;
    /*
    * @param file
    * @param location 前端解析的地址
    * @param *content 游记内容
    @return 插入游记的id/
     */
    @Override
    public int upload(MultipartFile file,String location,String content) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            MetaUtils.init(inputStream);
            Double latitude = MetaUtils.getLatitude();
            Double longitude = MetaUtils.getLongitude();
            Timestamp takenTime = MetaUtils.getDateTimeInfo();
            String make = MetaUtils.getMakeInfo();
            String model = MetaUtils.getModelInfo();
            String type = MetaUtils.getTypeInfo();
            String width = MetaUtils.getWidthInfo();
            String height = MetaUtils.getHeightInfo();
            String fnumber = MetaUtils.getFnumberInfo();
            String exposureTime = MetaUtils.getExposureTimeInfo();
            String iso = MetaUtils.getISOInfo();
            Double GCJ_lat = null;
            Double GCJ_lon = null;
            //WSG84坐标系转为GCJ-02坐标系
            if(latitude != null && longitude != null) {
                double[] to_gcj02 = gpsConverter.gps84_To_Gcj02(latitude, longitude);
                GCJ_lat = gpsConverter.retain6(to_gcj02[0]);
                GCJ_lon = gpsConverter.retain6(to_gcj02[1]);
            }
            String imagePath;
            if (file.getContentType().equals("image/heic") || file.getContentType().equals("image/heif")) {
                imagePath = imageStorageUtils.StorageHeif(file);
            }
            else{
                imagePath = imageStorageUtils.StorageImage(file);
            }
            Travel travel = new Travel(imagePath, content, latitude, longitude,GCJ_lat,GCJ_lon, takenTime, location, make, model, type, width, height, fnumber, exposureTime, iso);
            travelMapper.insert(travel);
            //返回插入游记的id
            return travel.getId();
        }
    }

    @Autowired
    GetLocationUtils getLocationUtils;

    @Override
    public int upload(String previewPath,Travel travel) throws Exception {
        String imagePath;
        imagePath = imageStorageUtils.StorageImage(previewPath);
        travel.setImagePath(imagePath);
        //没有提取到图片位置信息，根据用户自定义补全
        if(travel.getLatitude() == null || travel.getLongitude() == null ){
            double[] gps84 = gpsConverter.gcj02_To_Gps84(travel.getGcjLat(), travel.getGcjLon());
            travel.setLatitude(gpsConverter.retain6(gps84[0]));
            travel.setLongitude(gpsConverter.retain6(gps84[1]));
        }
        travelMapper.insert(travel);
        return  travel.getId();
    }

    @Override
    public List<Integer> listId(){
        List<Integer> ids = travelMapper.selectIds();
        return ids;
    }

    @Override
    public Travel getById(Integer id){
        Travel travel =  travelMapper.selectById(id);
        return travel;
    }

    @Override
    public List<GpsInfo> listGps() {
        List<GpsInfo> gpsList = travelMapper.selectGpsInfos();
        return gpsList;
    }

    @Override
    public List<ListInfo> listShort(Integer page) {
        PageHelper.startPage(page,pageSize);
        List<ListInfo> shortList = travelMapper.selectShortInfos();

        return shortList;
    }

    @Override
    public List<Integer> selectTravel(LocalDateTime startTime, LocalDateTime endTime, String location) {
        Timestamp start = Timestamp.valueOf(startTime);
        Timestamp end = Timestamp.valueOf(endTime);
        List<Integer> selectedTravels = travelMapper.selectByCase(start, end, location);
        return  selectedTravels;
    }

    @Override
    public List<Integer> genTra(Timestamp start, Timestamp end) {
        List<Integer> list=travelMapper.genTra(start,end);
        return list;
    }

    @Override
    public void updateLonLat(Integer id, Double gcjLat, Double gcjLon) {
        double[] gps84 = gpsConverter.gcj02_To_Gps84(gcjLat, gcjLon);
        Double lat = gpsConverter.retain6(gps84[0]);
        Double lon =gpsConverter.retain6(gps84[1]);
        travelMapper.updateLonLat(id,lat,lon,gcjLat,gcjLon);
    }

    @Override
    public void updateContent(Integer id, String content) {
        travelMapper.updateContent(id,content);
    }

    @Override
    public void deleteById(Integer id){
        Travel travel = travelMapper.selectById(id);
        String imagePath = travel.getImagePath();
        // 指定文件路径
        Path path = Paths.get(imagePath);
        try {
            // 删除文件
            Files.delete(path);
            System.out.println("文件已成功删除");
        } catch (NoSuchFileException e) {
            System.out.println("文件不存在");
        } catch (DirectoryNotEmptyException e) {
            System.out.println("该目录非空");
        } catch (IOException e) {
            System.out.println("删除文件时发生错误: " + e.getMessage());
        }
        travelMapper.deleteById(id);
    }

    @Override
    public void exportTravel(Integer id, boolean isRetainLocation, boolean isRetainTime, boolean isRetainParams, String exportType, String  exportPath) throws IOException {
        Travel travel = travelMapper.selectById(id);
        if(exportType.equals("PDF")){
            fileExport.exportToPDF(isRetainLocation,isRetainTime,isRetainParams,travel,exportPath);
        }
        else if(exportType.equals("PNG")){
            fileExport.exportToPNG(isRetainLocation,isRetainTime,isRetainParams,travel,exportPath);
        }

    }
}
