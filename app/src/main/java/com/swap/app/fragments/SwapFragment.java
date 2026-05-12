package com.swap.app.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.swap.app.R;
import com.swap.app.network.SupabaseClient;
import com.swap.app.utils.SessionManager;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SwapFragment extends Fragment {

    private RecyclerView recyclerSwaps;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private String currentUserId;

    private final List<JSONObject> swaps = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentUserId = SessionManager.getInstance(requireContext()).getUserId();

        recyclerSwaps = view.findViewById(R.id.recyclerSwaps);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerSwaps.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        view.findViewById(R.id.fabCreateSwap).setOnClickListener(v -> showCreateSwapDialog());

        loadSwaps();
    }

    private void loadSwaps() {
        progressBar.setVisibility(View.VISIBLE);
        String params = "is_active=eq.true&order=created_at.desc";
        SupabaseClient.getInstance().get("swaps", params, new SupabaseClient.ApiCallback() {
            @Override
            public void onSuccess(String body) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    swaps.clear();
                    try {
                        JSONArray arr = new JSONArray(body);
                        for (int i = 0; i < arr.length(); i++) swaps.add(arr.getJSONObject(i));
                        recyclerSwaps.setAdapter(new SwapGridAdapter());
                        tvEmpty.setVisibility(swaps.isEmpty() ? View.VISIBLE : View.GONE);
                    } catch (Exception e) {
                        tvEmpty.setVisibility(View.VISIBLE);
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

    private void showCreateSwapDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Create Swap");
        EditText input = new EditText(requireContext());
        input.setHint("What's on your mind?");
        input.setPadding(48, 24, 48, 24);
        builder.setView(input);
        builder.setPositiveButton("Post", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) postSwap(text);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void postSwap(String content) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", currentUserId);
            body.put("content", content);
            body.put("is_active", true);
            SupabaseClient.getInstance().post("swaps", body.toString(), new SupabaseClient.ApiCallback() {
                @Override public void onSuccess(String response) { requireActivity().runOnUiThread(() -> loadSwaps()); }
                @Override public void onError(String error) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private class SwapGridAdapter extends RecyclerView.Adapter<SwapGridAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(requireContext()).inflate(R.layout.item_swap, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            JSONObject swap = swaps.get(position);
            holder.tvContent.setText(swap.optString("content", ""));
            String mediaUrl = swap.optString("video_url", "");
            if (!mediaUrl.isEmpty()) {
                Glide.with(requireContext()).load(mediaUrl).centerCrop().into(holder.ivMedia);
                holder.ivMedia.setVisibility(View.VISIBLE);
            } else {
                holder.ivMedia.setVisibility(View.GONE);
            }
        }
        @Override public int getItemCount() { return swaps.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvContent;
            ImageView ivMedia;
            VH(View v) {
                super(v);
                tvContent = v.findViewById(R.id.tvContent);
                ivMedia = v.findViewById(R.id.ivMedia);
            }
        }
    }
}
