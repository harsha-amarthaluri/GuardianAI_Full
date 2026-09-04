package com.guardianai.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.guardianai.R;

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

        // Intelligent AI Response Engine
        String lower = userText.toLowerCase();
        if (lower.contains("sos") || lower.contains("emergency")) {
            addBotMessage("Emergency SOS sends your live GPS coordinates to the Guardian AI backend and your trusted guardians immediately. You can trigger it manually via the dashboard red SOS button or by shaking your device rapidly.");
        } else if (lower.contains("score") || lower.contains("safety")) {
            addBotMessage("Your Safety Score (0-100) is calculated dynamically by querying nearby active crime & threat events in Supabase PostgreSQL, evaluating late-night risk penalties (22:00-05:00), and tracking accuracy.");
        } else if (lower.contains("guardian")) {
            addBotMessage("You can manage guardians in the Guardians tab. When an emergency alert or low-battery (<15%) warning occurs, guardians receive real-time notifications.");
        } else if (lower.contains("route") || lower.contains("map")) {
            addBotMessage("On the Map tab, tap the '🚦 Safe Route' button to enter a destination. The system evaluates candidate routes against threat density and recommends the safest path.");
        } else {
            addBotMessage("I am monitoring your situational safety. For immediate danger, tap the red EMERGENCY SOS button on your home dashboard.");
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
