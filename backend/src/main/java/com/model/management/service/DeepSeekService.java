package com.model.management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.management.config.DeepSeekConfig;
import com.model.management.dto.DeepSeekRequestDTO;
import com.model.management.dto.DeepSeekResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DeepSeekConfig deepSeekConfig;
    private final RestTemplate restTemplate;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(List<DeepSeekRequestDTO.Message> history, String currentMessage) {
        try {
            DeepSeekRequestDTO request = new DeepSeekRequestDTO();
            request.setModel(deepSeekConfig.getModel());
            request.setMessages(buildMessages(currentMessage, history));
            request.setTemperature(deepSeekConfig.getTemperature());
            request.setMax_tokens(deepSeekConfig.getMaxTokens());
            request.setStream(false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekConfig.getApiKey());

            HttpEntity<DeepSeekRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<DeepSeekResponseDTO> response = restTemplate.exchange(
                    deepSeekConfig.getBaseUrl() + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    DeepSeekResponseDTO.class
            );

            if (response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
                return response.getBody().getChoices().get(0).getMessage().getContent();
            }

            return "抱歉，AI 助手暂时无法回复。";
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage());
        }
    }

    public void chatStream(List<DeepSeekRequestDTO.Message> history, String currentMessage,
                          Consumer<String> onMessage, Runnable onComplete, Consumer<Throwable> onError) {
        Thread thread = new Thread(() -> {
            try {
                DeepSeekRequestDTO request = new DeepSeekRequestDTO();
                request.setModel(deepSeekConfig.getModel());
                request.setMessages(buildMessages(currentMessage, history));
                request.setTemperature(deepSeekConfig.getTemperature());
                request.setMax_tokens(deepSeekConfig.getMaxTokens());
                request.setStream(true);

                String jsonBody = objectMapper.writeValueAsString(request);

                RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json; charset=utf-8"));
                Request okRequest = new Request.Builder()
                        .url(deepSeekConfig.getBaseUrl() + "/chat/completions")
                        .post(body)
                        .addHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .build();

                try (Response response = okHttpClient.newCall(okRequest).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        log.error("DeepSeek API 返回错误: {} - {}", response.code(), errorBody);
                        onError.accept(new RuntimeException("API 请求失败: " + response.code()));
                        return;
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            onComplete.run();
                            return;
                        }

                        var source = responseBody.source();
                        
                        while (!source.exhausted()) {
                            String line = source.readUtf8Line();
                            if (line == null || !line.startsWith("data: ")) {
                                continue;
                            }

                            String data = line.substring(6);
                            
                            if ("[DONE]".equals(data)) {
                                break;
                            }

                            try {
                                JsonNode jsonNode = objectMapper.readTree(data);
                                JsonNode choices = jsonNode.get("choices");
                                if (choices != null && choices.isArray() && choices.size() > 0) {
                                    JsonNode delta = choices.get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        String content = delta.get("content").asText();
                                        onMessage.accept(content);
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("解析 SSE 数据失败: {}", data);
                            }
                        }
                    }

                    onComplete.run();
                }
            } catch (Exception e) {
                log.error("流式调用 DeepSeek API 失败: {}", e.getMessage(), e);
                onError.accept(e);
            }
        });

        thread.start();
    }

    public List<DeepSeekRequestDTO.Message> buildMessages(String currentMessage, List<DeepSeekRequestDTO.Message> history) {
        List<DeepSeekRequestDTO.Message> messages = new ArrayList<>();
        
        DeepSeekRequestDTO.Message systemMessage = new DeepSeekRequestDTO.Message();
        systemMessage.setRole("system");
        systemMessage.setContent("你是 ModelSphere 拼装模型管理系统的 AI 智能助手。你需要：\n" +
                "1. 帮助用户解答关于拼装模型的问题 - 包括模型管理、使用说明、制作技巧等\n" +
                "2. 能分点回答尽量分点回答\n" +
                "3. 用简单、直接、利索的语言回答问题\n" +
                "4. 不要使用 Markdown 格式，只用纯文本回答\n" +
                "5. 回答要简洁明了，不要用太多格式和修饰\n" +
                "6. 如果遇到无法解答的问题，诚实地告知用户");
        messages.add(systemMessage);
        
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        
        DeepSeekRequestDTO.Message userMsg = new DeepSeekRequestDTO.Message();
        userMsg.setRole("user");
        userMsg.setContent(currentMessage);
        messages.add(userMsg);
        
        return messages;
    }

    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t);
    }
}
