package com.example.cookshare.services;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private static final String API_KEY = "sk-abc";
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

    /**
     * Gửi message với lịch sử chat để giữ context
     * 
     * @param chatHistory Danh sách các message trong cuộc hội thoại (không bao gồm
     *                    welcome message)
     * @param callback    Callback để nhận kết quả
     */
    public void sendMessage(List<com.example.cookshare.models.ChatMessage> chatHistory, ChatCallback callback) {
        if (API_KEY.equals("YOUR_OPENAI_API_KEY_HERE") || API_KEY.isEmpty()) {
            callback.onError("Vui lòng cấu hình OpenAI API key trong OpenAIService.java");
            return;
        }

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");
            requestBody.addProperty("max_tokens", 800); // Tăng lên để có thể trả lời dài hơn khi có context
            requestBody.addProperty("temperature", 0.7);

            // System prompt for Vietnamese food assistant with context
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");

            // Import ContextHelper để lấy thông tin context
            String contextInfo = com.example.cookshare.utils.ContextHelper.getFullContext();

            systemMessage.addProperty("content",
                    "Bạn là một trợ lý ẩm thực chuyên về món ăn Việt Nam. " +
                            "Hãy trả lời bằng tiếng Việt và tập trung vào:\n" +
                            "- Tư vấn món ăn Việt Nam\n" +
                            "- Hướng dẫn nấu ăn\n" +
                            "- Gợi ý nguyên liệu\n" +
                            "- Cách pha chế gia vị\n" +
                            "- Lịch sử và văn hóa ẩm thực Việt\n" +
                            "- Gợi ý món ăn dựa trên thời tiết, thứ trong tuần, buổi trong ngày\n\n" +
                            contextInfo + "\n" +
                            "QUAN TRỌNG: Bạn cần nhớ và giữ context của cuộc hội thoại. " +
                            "Khi người dùng hỏi tiếp về món ăn đang nói đến, hãy tiếp tục cuộc hội thoại về món đó, " +
                            "KHÔNG nhảy sang gợi ý món khác trừ khi người dùng yêu cầu.\n\n" +
                            "Khi người dùng hỏi về:\n" +
                            "- Thời tiết: Bạn có thể tham khảo thông tin thời tiết ở trên\n" +
                            "- Ngày tháng/thứ trong tuần: Sử dụng thông tin ngày tháng ở trên\n" +
                            "- 'Hôm nay nên ăn gì?': Gợi ý món ăn phù hợp với thời tiết, buổi trong ngày, và thứ trong tuần\n"
                            +
                            "- 'Thời tiết như này nên ăn gì?': Đưa ra gợi ý món ăn phù hợp với thời tiết hiện tại\n" +
                            "- Câu hỏi tiếp theo về món đang nói: Tiếp tục về món đó, trả lời chi tiết hơn\n\n" +
                            "Hãy trả lời ngắn gọn, dễ hiểu và thân thiện. Luôn tích hợp thông tin context vào câu trả lời khi phù hợp. "
                            +
                            "Luôn nhớ context của cuộc hội thoại trước đó.");

            // Build messages array với system message + chat history
            List<JsonObject> messagesList = new ArrayList<>();
            messagesList.add(systemMessage);

            // Giới hạn số lượng messages để tránh vượt quá token limit
            // Giữ 20 messages gần nhất (10 cặp user-assistant)
            int maxMessages = 20;
            int startIndex = Math.max(0, chatHistory.size() - maxMessages);

            // Convert chat history thành format OpenAI
            for (int i = startIndex; i < chatHistory.size(); i++) {
                com.example.cookshare.models.ChatMessage chatMsg = chatHistory.get(i);
                JsonObject msgObj = new JsonObject();
                msgObj.addProperty("role", chatMsg.isUser() ? "user" : "assistant");
                msgObj.addProperty("content", chatMsg.getMessage());
                messagesList.add(msgObj);
            }

            // Convert to array
            JsonObject[] messages = messagesList.toArray(new JsonObject[0]);
            requestBody.add("messages", gson.toJsonTree(messages));

            Log.d(TAG, "Sending " + messages.length + " messages to OpenAI (including system message)");

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
