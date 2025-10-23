package com.example.cookshare.services;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIService {
    private static final String TAG = "OpenAIService";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    // OpenAI API key
    private static final String API_KEY = "abc"
    private OkHttpClient client;
    private Gson gson;

    public OpenAIService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public interface ChatCallback {
        void onSuccess(String response);

        void onError(String error);
    }

    public void sendMessage(String userMessage, ChatCallback callback) {
        if (API_KEY.equals("YOUR_OPENAI_API_KEY_HERE")) {
            callback.onError("Vui lòng cấu hình OpenAI API key trong OpenAIService.java");
            return;
        }

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");
            requestBody.addProperty("max_tokens", 500);
            requestBody.addProperty("temperature", 0.7);

            // System prompt for Vietnamese food assistant
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content",
                    "Bạn là một trợ lý ẩm thực chuyên về món ăn Việt Nam. " +
                            "Hãy trả lời bằng tiếng Việt và tập trung vào:\n" +
                            "- Tư vấn món ăn Việt Nam\n" +
                            "- Hướng dẫn nấu ăn\n" +
                            "- Gợi ý nguyên liệu\n" +
                            "- Cách pha chế gia vị\n" +
                            "- Lịch sử và văn hóa ẩm thực Việt\n\n" +
                            "Hãy trả lời ngắn gọn, dễ hiểu và thân thiện.");

            JsonObject userMessageObj = new JsonObject();
            userMessageObj.addProperty("role", "user");
            userMessageObj.addProperty("content", userMessage);

            JsonObject[] messages = { systemMessage, userMessageObj };
            requestBody.add("messages", gson.toJsonTree(messages));

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    gson.toJson(requestBody));

            Request request = new Request.Builder()
                    .url(OPENAI_API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "OpenAI API call failed", e);
                    callback.onError("Không thể kết nối đến OpenAI API: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        Log.e(TAG, "OpenAI API error: " + response.code() + " - " + errorBody);
                        callback.onError("Lỗi API: " + response.code() + " - " + errorBody);
                        return;
                    }

                    String responseBody = response.body().string();
                    Log.d(TAG, "OpenAI API response: " + responseBody);

                    try {
                        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                        if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                            JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                            JsonObject message = choice.getAsJsonObject("message");
                            String content = message.get("content").getAsString();

                            callback.onSuccess(content);
                        } else {
                            callback.onError("Không nhận được phản hồi từ AI");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing OpenAI response", e);
                        callback.onError("Lỗi xử lý phản hồi: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating OpenAI request", e);
            callback.onError("Lỗi tạo yêu cầu: " + e.getMessage());
        }
    }
}
