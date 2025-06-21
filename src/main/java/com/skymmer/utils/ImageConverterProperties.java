package com.skymmer.utils;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app.svf")
public class ImageConverterProperties {
    private String tempDir;
    private String baseUrl;
    private int cleanupDelayMinutes;
}
