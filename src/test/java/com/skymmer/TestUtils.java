package com.skymmer;

import com.skymmer.mapper.TravelMapper;
import com.skymmer.pojo.Travel;
import com.skymmer.utils.FileExport;
import com.skymmer.utils.GetLocationUtils;
import com.skymmer.utils.GpsConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

@SpringBootTest
public class TestUtils {
    @Autowired
    GpsConverter gpsConverter;
    @Autowired
    GetLocationUtils getLocationUtils;
    @Test
    public void testGetLocation() throws URISyntaxException, IOException, InterruptedException {
        Double lat=30.515605;
        Double lon=114.431808;

        double[] to_gcj02 = gpsConverter.gps84_To_Gcj02(lat, lon);
        Double gcjlat=to_gcj02[0];
        Double gcjlon=to_gcj02[1];

        String body = getLocationUtils.getLocation(gcjlat,gcjlon);
        System.out.println(body);

    }

    @Autowired
    TravelMapper travelMapper;

    @Autowired
    FileExport fileExport;

    @Test
    public void testExportToPDF() throws IOException {
        Travel travel = travelMapper.selectById(1);
        fileExport.exportToPDF(true,true,true,travel,null);
    }

    @Test
    public void testExportToPNG() throws IOException {
        Travel travel = travelMapper.selectById(1);
        fileExport.exportToPNG(true,true,true,travel,null);

    }
}
