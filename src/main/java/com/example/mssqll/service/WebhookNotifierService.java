package com.example.mssqll.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookNotifierService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    @Async
    public void sendExceptionNotification(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("```");
        sb.append("Exception: ").append(ex.getClass().getName()).append("\n");
        sb.append("Message: ").append(ex.getMessage()).append("\n");
        sb.append("```").append("\n");

        if (ex.getStackTrace().length > 0) {
            StackTraceElement origin = ex.getStackTrace()[0];
            sb.append("*Occurred at:* ").append(origin.toString()).append("\n\n");
        }

        sb.append("Full stack trace:\n```");
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        sb.append("```");
        Map<String, String> payload = new HashMap<>();
        payload.put("text", sb.toString());

        try {
            restTemplate.postForEntity(webhookUrl, payload, String.class);
        } catch (Exception e) {
            System.err.println("error sending to webhook: " + e.getMessage());
        }

    }

}
