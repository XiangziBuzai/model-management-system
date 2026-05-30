package com.model.management.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeepSeekRequestDTO {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
