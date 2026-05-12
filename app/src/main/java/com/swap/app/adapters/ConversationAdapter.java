package com.swap.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.swap.app.R;
import com.swap.app.models.Conversation;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    public interface OnConversationClickListener {
        void onClick(Conversation conversation);
    }

    private final Context context;
    private final List<Conversation> conversations;
    private final OnConversationClickListener listener;

    public ConversationAdapter(Context ctx, List<Conversation> list, OnConversationClickListener l) {
        this.context = ctx;
        this.conversations = list;
        this.listener = l;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation c = conversations.get(position);
        holder.tvName.setText(c.otherUserName != null ? c.otherUserName : "Unknown");
        holder.tvLastMessage.setText(c.lastMessage != null ? c.lastMessage : "");
        if (c.avatarUrl != null && !c.avatarUrl.isEmpty()) {
            Glide.with(context).load(c.avatarUrl).circleCrop().into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
        holder.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override public int getItemCount() { return conversations.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvLastMessage;
        ViewHolder(View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvName = v.findViewById(R.id.tvName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
        }
    }
              }
