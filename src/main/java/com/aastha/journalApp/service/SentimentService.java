package com.aastha.journalApp.service;

import com.aastha.journalApp.dto.SentimentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Slf4j
@Service
public class SentimentService {

    private static final String apiURL = "https://api.apilayer.com/sentiment/analysis";
    private static final String apiKey = "XXedL8ttOghi2AKFD0wiggWNv1pJtvt5";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public SentimentResponse analyzeSentiment(String inputText) {
        log.info("Analyzing sentiment for input text: {}", inputText);

        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, inputText);

        // Build request
        Request request = new Request.Builder()
                .url(apiURL)
                .method("POST", body)
                .addHeader("apikey", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                String json = response.body().string();
                log.debug("Raw sentiment API response: {}", json);

                SentimentResponse result = mapper.readValue(json, SentimentResponse.class);
                log.info("Sentiment: {}, Score: {}, Confidence: {}%", result.getSentiment(), result.getScore(), result.getConfidence());
                return result;
            } else {
                log.warn("Empty response body received from sentiment API");
                return null;
            }
        } catch (IOException e) {
            log.error("Exception occurred during sentiment analysis", e);
            return null;
        }
    }
}
