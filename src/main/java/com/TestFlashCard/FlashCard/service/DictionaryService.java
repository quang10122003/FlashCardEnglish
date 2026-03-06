package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.response.CardFillResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DictionaryService {
    private static final String TRANSLITERATION_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public CardFillResponse fetchWordData(String word) {
        RestTemplate restTemplate = createRestTemplate();

        String apiUrl = TRANSLITERATION_API_URL + word;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Referer", "https://dictionaryapi.dev/");
        headers.set("Origin", "https://dictionaryapi.dev");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> response = responseEntity.getBody();

            if (response == null || response.isEmpty()) {
                return new CardFillResponse("", "", "", List.of(), List.of(), "");
            }

            String phoneticText = "";
            String audioUrl = "";

            Map<String, String> hintMap = new LinkedHashMap<>();    // partOfSpeech -> definition
            Map<String, String> exampleMap = new LinkedHashMap<>(); // partOfSpeech -> example

            for (Map<String, Object> entry : response) {
                // === Lấy phonetics (text + audio) ===
                List<Map<String, Object>> phonetics = (List<Map<String, Object>>) entry.get("phonetics");
                if (phonetics != null) {
                    for (Map<String, Object> phonetic : phonetics) {
                        if (phoneticText.isBlank()) {
                            Object textObj = phonetic.get("text");
                            if (textObj instanceof String text && !text.isBlank()) {
                                phoneticText = text;
                            }
                        }
                        if (audioUrl.isBlank()) {
                            Object audioObj = phonetic.get("sourceUrl");
                            if (audioObj instanceof String audio && !audio.isBlank()) {
                                audioUrl = audio;
                            }
                        }
                        if (!phoneticText.isBlank() && !audioUrl.isBlank()) break;
                    }
                }

                // === Lấy meanings ===
                List<Map<String, Object>> meanings = (List<Map<String, Object>>) entry.get("meanings");
                if (meanings == null) continue;

                for (Map<String, Object> meaning : meanings) {
                    Object posObj = meaning.get("partOfSpeech");
                    if (!(posObj instanceof String pos) || pos.isBlank()) continue;
                    String partOfSpeech = pos;

                    // Nếu đã có definition cho partOfSpeech này thì bỏ qua
                    if (hintMap.containsKey(partOfSpeech)) continue;

                    List<Map<String, Object>> definitions = (List<Map<String, Object>>) meaning.get("definitions");
                    if (definitions == null || definitions.isEmpty()) continue;

                    // 1) Tìm definition có example
                    boolean found = false;
                    for (Map<String, Object> def : definitions) {
                        String defStr = def.get("definition") instanceof String ? (String) def.get("definition") : "";
                        String exStr = def.get("example") instanceof String ? (String) def.get("example") : "";
                        if (!defStr.isBlank() && !exStr.isBlank()) {
                            hintMap.put(partOfSpeech, defStr);
                            exampleMap.put(partOfSpeech, exStr);
                            found = true;
                            break;
                        }
                    }
                    if (found) continue;

                    // 2) Nếu không có example, lấy definition đầu tiên
                    for (Map<String, Object> def : definitions) {
                        String defStr = def.get("definition") instanceof String ? (String) def.get("definition") : "";
                        if (!defStr.isBlank()) {
                            hintMap.put(partOfSpeech, defStr);
                            exampleMap.put(partOfSpeech, "");
                            break;
                        }
                    }
                }
            }

            // === Chuẩn hóa dữ liệu đầu ra ===
            String partOfSpeech = !hintMap.isEmpty() ? String.join(", ", hintMap.keySet()) : "";
            String hint = !hintMap.isEmpty() ? String.join("+", hintMap.values()) : "";
            String example = !exampleMap.isEmpty()
                    ? String.join("+", exampleMap.values())
                    : "";

            List<String> hintList = hint.isEmpty() ? List.of() : Arrays.stream(hint.split("\\+")).toList();
            List<String> exampleList = example.isEmpty() ? List.of() : Arrays.stream(example.split("\\+")).toList();

            return new CardFillResponse(
                    phoneticText,
                    audioUrl,
                    partOfSpeech,
                    exampleList,
                    hintList,
                    ""
            );

        } catch (Exception e) {
            log.error("Error when fetching word data: {}", e.getMessage());
            return new CardFillResponse("", "", "", List.of(), List.of(), "");
        }
    }
    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5s
        factory.setReadTimeout(10000);   // 10s
        return new RestTemplate(factory);
    }
}
