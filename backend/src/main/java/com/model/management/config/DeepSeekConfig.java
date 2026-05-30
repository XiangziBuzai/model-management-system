package com.model.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    private String apiKey;
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;
}
