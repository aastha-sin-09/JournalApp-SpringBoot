package com.aastha.journalApp.dto;

import lombok.Data;

@Data
public class SentimentResponse {
    private String sentiment;
    private int score;
    private double confidence;
    private String language;
    private String content_type;
}
