package com.guardianai.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.guardianai.R;
import com.guardianai.data.api.ApiClient;
import com.guardianai.data.models.FeatureDtos;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportChatActivity extends AppCompatActivity {

    private LinearLayout containerChatMessages;
    private EditText editChatMessage;
    private Button btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat);

        containerChatMessages = findViewById(R.id.containerChatMessages);
        editChatMessage = findViewById(R.id.editChatMessage);
        btnSendMessage = findViewById(R.id.btnSendChatMessage);

        btnSendMessage.setOnClickListener(v -> sendMessage());

        // Welcome Bot Message
        addBotMessage("Hello! I am your Guardian AI Safety Assistant. How can I help you stay safe today?\n\nSuggested questions:\n• How does Emergency SOS work?\n• What is my safety score based on?\n• How to add trusted guardians?");
    }

    private void sendMessage() {
        String userText = editChatMessage.getText().toString().trim();
        if (userText.isEmpty()) return;

        addUserMessage(userText);
        editChatMessage.setText("");

        // Show typing indicator
        addBotMessage("Guardian AI is analyzing your query...");

        FeatureDtos.ChatRequestDto req = new FeatureDtos.ChatRequestDto(userText);
        ApiClient.getApiService(this).sendChatMessage(req).enqueue(new Callback<FeatureDtos.ChatResponseDto>() {
            @Override
            public void onResponse(Call<FeatureDtos.ChatResponseDto> call, Response<FeatureDtos.ChatResponseDto> response) {
                removeLastBotMessageIfLoading();
                if (response.isSuccessful() && response.body() != null) {
                    FeatureDtos.ChatResponseDto resData = response.body();
                    addBotMessage(resData.getReply());
                    if (resData.isEmergencyDetected()) {
                        Toast.makeText(SupportChatActivity.this, "⚠️ Distress Detected: Tap Emergency SOS on Home Dashboard for immediate help!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    fallbackLocalResponse(userText);
                }
            }

            @Override
            public void onFailure(Call<FeatureDtos.ChatResponseDto> call, Throwable t) {
                removeLastBotMessageIfLoading();
                fallbackLocalResponse(userText);
            }
        });
    }

    private void removeLastBotMessageIfLoading() {
        int count = containerChatMessages.getChildCount();
        if (count > 0) {
            View lastView = containerChatMessages.getChildAt(count - 1);
            if (lastView instanceof TextView) {
                TextView tv = (TextView) lastView;
                if (tv.getText().toString().contains("analyzing your query")) {
                    containerChatMessages.removeViewAt(count - 1);
                }
            }
        }
    }

    private void fallbackLocalResponse(String userText) {
        String lower = userText.toLowerCase();
        if (lower.contains("sos") || lower.contains("emergency")) {
            addBotMessage("Emergency SOS sends your live GPS coordinates to the Guardian AI backend and your trusted guardians immediately. You can trigger it manually via the dashboard red SOS button or by shaking your device rapidly.");
        } else if (lower.contains("score") || lower.contains("safety")) {
            addBotMessage("Your Safety Score (0-100) is calculated dynamically by querying nearby active crime & threat events, evaluating late-night risk penalties (22:00-05:00), and tracking accuracy.");
        } else if (lower.contains("guardian")) {
            addBotMessage("You can manage guardians in the Guardians tab. When an emergency alert or low-battery (<15%) warning occurs, guardians receive real-time notifications.");
        } else if (lower.contains("route") || lower.contains("map")) {
            addBotMessage("On the Map tab, tap the '🚦 Safe Route' button to enter a destination. The system evaluates candidate routes against threat density and recommends the safest path.");
        } else {
            addBotMessage("Guardian AI is monitoring your situational safety. For immediate danger, tap the red EMERGENCY SOS button on your home dashboard.");
        }
    }

    private void addUserMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(14);
        textView.setPadding(16, 12, 16, 12);
        textView.setBackgroundColor(Color.parseColor("#0284C7"));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.END;
        params.setMargins(40, 8, 0, 8);
        textView.setLayoutParams(params);

        containerChatMessages.addView(textView);
    }

    private void addBotMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(Color.parseColor("#0F172A"));
        textView.setTextSize(14);
        textView.setPadding(16, 12, 16, 12);
        textView.setBackgroundColor(Color.parseColor("#E2E8F0"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.START;
        params.setMargins(0, 8, 40, 8);
        textView.setLayoutParams(params);

        containerChatMessages.addView(textView);
    }
}
