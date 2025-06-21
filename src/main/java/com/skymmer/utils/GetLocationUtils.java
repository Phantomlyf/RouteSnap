package com.skymmer.utils;


import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



@Component
public class GetLocationUtils {
    // 创建HttpClient对象
   public   String getLocation(Double lat,Double lon) throws URISyntaxException, IOException, InterruptedException {
       HttpClient client = HttpClient.newHttpClient();
       String parm = "location="+lon+","+lat+"&key=a2530aeba83e6f96d48b2f96d2eb0824";
       URI uri=URI.create("https://restapi.amap.com/v3/geocode/regeo?" + parm);
       // 创建HttpRequest对象
       HttpRequest request = HttpRequest.newBuilder()
               .uri(uri)
               .GET()
               .build();

       // 发送请求并接收响应
       HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

       // 输出响应内容
       System.out.println("Response Code: " + response.statusCode());
       return extractFormattedAddress(response.body());

   }

    private static String extractFormattedAddress(String json) {
        // 查找格式化地址字段
        String targetKey = "\"formatted_address\":\"";
        int startIndex = json.indexOf(targetKey);

        if (startIndex == -1) {
            return "未找到地址信息";
        }

        startIndex += targetKey.length();
        int endIndex = json.indexOf("\"", startIndex);

        if (endIndex == -1 || endIndex <= startIndex) {
            return "地址格式解析错误";
        }
        return json.substring(startIndex, endIndex);
    }
}
