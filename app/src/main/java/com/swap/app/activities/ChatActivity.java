package com.swap.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swap.app.R;
import com.swap.app.adapters.MessageAdapter;
import com.swap.app.models.Message;
import com.swap.app.network.SupabaseClient;
import com.swap.app.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_OTHER_USER_NAME = "other_user_name";

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private String conversationId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        String otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        currentUserId = SessionManager.getInstance(this).getUserId();

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        adapter = new MessageAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
        loadMessages();
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        String params = "conversation_id=eq." + conversationId + "&order=created_at.asc";
        SupabaseClient.getInstance().get("messages", params, new SupabaseClient.ApiCallback() {
            @Override
            public void onSuccess(String body) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    messages.clear();
                    try {
                        JSONArray arr = new JSONArray(body);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Message msg = new Message();
                            msg.id = obj.optString("id");
                            msg.content = obj.optString("content");
                            msg.senderId = obj.optString("sender_id");
                            msg.conversationId = obj.optString("conversation_id");
                            msg.createdAt = obj.optString("created_at");
                            messages.add(msg);
                        }
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        etMessage.setText("");

        try {
            JSONObject body = new JSONObject();
            body.put("conversation_id", conversationId);
            body.put("sender_id", currentUserId);
            body.put("content", text);
            body.put("message_type", "text");

            SupabaseClient.getInstance().post("messages", body.toString(), new SupabaseClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> loadMessages());
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
                                           }
