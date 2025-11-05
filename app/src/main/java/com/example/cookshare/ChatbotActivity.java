package com.example.cookshare;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookshare.adapters.ChatAdapter;
import com.example.cookshare.models.ChatMessage;
import com.example.cookshare.services.OpenAIService;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {
    private static final String TAG = "ChatbotActivity";

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ProgressBar progressBar;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private OpenAIService openAIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Setup toolbar with back button
        setupToolbar();

        initViews();
        setupRecyclerView();
        setupClickListeners();

        openAIService = new OpenAIService();

        // Add welcome message
        addWelcomeMessage();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Trợ lý ẩm thực");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Chỉ finish activity, không gọi onBackPressed()
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Chỉ finish activity
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void addWelcomeMessage() {
        // Lấy thông tin context
        String dayOfWeek = com.example.cookshare.utils.ContextHelper.getDayOfWeek();
        String weather = com.example.cookshare.utils.ContextHelper.getWeatherInfo();
        String timeOfDay = com.example.cookshare.utils.ContextHelper.getTimeOfDay();

        ChatMessage welcomeMessage = new ChatMessage(
                "Xin chao! Toi la tro ly am thuc CookShare. Toi co the giup ban:\n\n" +
                        "- Tu van mon an Viet Nam\n" +
                        "- Huong dan nau an\n" +
                        "- Goi y nguyen lieu\n" +
                        "- Cach pha che gia vi\n" +
                        "- Goi y mon an theo thoi tiet\n" +
                        "- Goi y mon an theo ngay trong tuan\n\n" +
                        "Hom nay la " + dayOfWeek + ", thoi tiet " + weather +
                        ". Ban co the hoi toi:\n" +
                        "- 'Hom nay nen an gi?'\n" +
                        "- 'Thoi tiet nhu nay nen an gi?'\n" +
                        "- 'Hom nay thu may?'\n" +
                        "- Hoac bat cu cau hoi nao ve am thuc!",
                false);
        chatMessages.add(welcomeMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Add user message
        ChatMessage userMessage = new ChatMessage(message, true);
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        // Clear input
        messageEditText.setText("");

        // Show loading
        showLoading();

        // Send to OpenAI với toàn bộ lịch sử chat (trừ welcome message)
        // Tạo danh sách chat history không bao gồm welcome message
        List<ChatMessage> chatHistory = new ArrayList<>();
        for (int i = 0; i < chatMessages.size(); i++) {
            ChatMessage msg = chatMessages.get(i);
            // Bỏ qua welcome message (message đầu tiên, không phải từ user)
            if (i > 0 || msg.isUser()) {
                chatHistory.add(msg);
            }
        }

        // Send to OpenAI với chat history
        openAIService.sendMessage(chatHistory, new OpenAIService.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    hideLoading();

                    // Add bot response
                    ChatMessage botMessage = new ChatMessage(response, false);
                    chatMessages.add(botMessage);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();

                    ChatMessage errorMessage = new ChatMessage(
                            "Xin lỗi, tôi gặp lỗi khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.",
                            false);
                    chatMessages.add(errorMessage);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.scrollToPosition(chatMessages.size() - 1);

                    Toast.makeText(ChatbotActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        sendButton.setEnabled(true);
    }
}
