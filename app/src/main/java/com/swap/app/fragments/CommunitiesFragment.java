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
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CommunitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<JSONObject> communities = new ArrayList<>();
    private String currentUserId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_communities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentUserId = SessionManager.getInstance(requireContext()).getUserId();

        recyclerView = view.findViewById(R.id.recyclerCommunities);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.fabCreateCommunity).setOnClickListener(v -> showCreateDialog());

        loadCommunities();
    }

    private void loadCommunities() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseClient.getInstance().get("communities", "order=created_at.desc", new SupabaseClient.ApiCallback() {
            @Override
            public void onSuccess(String body) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    communities.clear();
                    try {
                        JSONArray arr = new JSONArray(body);
                        for (int i = 0; i < arr.length(); i++) communities.add(arr.getJSONObject(i));
                        recyclerView.setAdapter(new CommunityAdapter());
                        tvEmpty.setVisibility(communities.isEmpty() ? View.VISIBLE : View.GONE);
                    } catch (Exception e) { tvEmpty.setVisibility(View.VISIBLE); }
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

    private void showCreateDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Create Community");
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);
        EditText etName = new EditText(requireContext()); etName.setHint("Community name"); layout.addView(etName);
        EditText etDesc = new EditText(requireContext()); etDesc.setHint("Description"); layout.addView(etDesc);
        builder.setView(layout);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) createCommunity(name, etDesc.getText().toString().trim());
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createCommunity(String name, String desc) {
        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("description", desc);
            body.put("created_by", currentUserId);
            SupabaseClient.getInstance().post("communities", body.toString(), new SupabaseClient.ApiCallback() {
                @Override public void onSuccess(String r) { requireActivity().runOnUiThread(() -> loadCommunities()); }
                @Override public void onError(String e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), e, Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) { Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show(); }
    }

    private class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(requireContext()).inflate(R.layout.item_community, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JSONObject c = communities.get(pos);
            h.tvName.setText(c.optString("name", ""));
            h.tvDesc.setText(c.optString("description", ""));
            h.btnJoin.setOnClickListener(v -> joinCommunity(c.optString("id")));
        }
        @Override public int getItemCount() { return communities.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDesc; Button btnJoin;
            VH(View v) { super(v); tvName = v.findViewById(R.id.tvName); tvDesc = v.findViewById(R.id.tvDesc); btnJoin = v.findViewById(R.id.btnJoin); }
        }
    }

    private void joinCommunity(String communityId) {
        try {
            JSONObject body = new JSONObject();
            body.put("community_id", communityId);
            body.put("user_id", currentUserId);
            body.put("role", "member");
            SupabaseClient.getInstance().post("community_members", body.toString(), new SupabaseClient.ApiCallback() {
                @Override public void onSuccess(String r) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Joined!", Toast.LENGTH_SHORT).show());
                }
                @Override public void onError(String e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), e, Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) { Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show(); }
    }
}
