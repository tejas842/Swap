package com.swap.app.adapters;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swap.app.R;
import com.swap.app.models.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int VIEW_SENT = 1;
    private static final int VIEW_RECEIVED = 2;

    private final Context context;
    private final List<Message> messages;
    private final String currentUserId;

    public MessageAdapter(Context ctx, List<Message> msgs, String userId) {
        this.context = ctx;
        this.messages = msgs;
        this.currentUserId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).senderId.equals(currentUserId) ? VIEW_SENT : VIEW_RECEIVED;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_SENT ? R.layout.item_message_sent : R.layout.item_message_received;
        View v = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvMessage.setText(messages.get(position).content);
    }

    @Override public int getItemCount() { return messages.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        ViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
        }
    }
}
