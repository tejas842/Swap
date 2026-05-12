package com.swap.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.swap.app.R;
import com.swap.app.activities.ChatActivity;
import com.swap.app.adapters.ConversationAdapter;
import com.swap.app.models.Conversation;
import com.swap.app.network.SupabaseClient;
import com.swap.app.utils.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ConversationAdapter adapter;
    private List<Conversation> conversations = new ArrayList<>();
    private List<Conversation> filteredList = new ArrayList<>();
    private String currentUserId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentUserId = SessionManager.getInstance(requireContext()).getUserId();

        recyclerView = view.findViewById(R.id.recyclerConversations);
        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new ConversationAdapter(requireContext(), filteredList, conversation -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.id);
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, conversation.otherUserName);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterList(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadConversations();
    }

    private void loadConversations() {
        progressBar.setVisibility(View.VISIBLE);
        String params = "select=*,conversation_participants!inner(user_id)&conversation_participants.user_id=eq." + currentUserId + "&order=updated_at.desc";
        SupabaseClient.getInstance().get("conversations", params, new SupabaseClient.ApiCallback() {
            @Override
            public void onSuccess(String body) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    conversations.clear();
                    try {
                        JSONArray arr = new JSONArray(body);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            Conversation c = new Conversation();
                            c.id = obj.optString("id");
                            c.lastMessage = obj.optString("last_message_preview", "");
                            c.lastMessageTime = obj.optString("updated_at");
                            c.isGroup = obj.optBoolean("is_group", false);
                            c.otherUserName = obj.optString("name", "Conversation");
                            conversations.add(c);
                        }
                        filterList(etSearch.getText().toString());
                        tvEmpty.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error loading chats", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(conversations);
        } else {
            String q = query.toLowerCase();
            for (Conversation c : conversations) {
                if (c.otherUserName != null && c.otherUserName.toLowerCase().contains(q)) {
                    filteredList.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
