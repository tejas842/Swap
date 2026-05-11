package com.swap.app.network;

import android.util.Log;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SupabaseClient {

    private static final String TAG = "SupabaseClient";
    public static final String SUPABASE_URL = "https://dncayzzxqozkqitspkne.supabase.co";
    public static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRuY2F5enp4cW96a3FpdHNwa25lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcxNTU3ODEsImV4cCI6MjA3MjczMTc4MX0.INMKBUgGuXyW5h_eIykutG-zpmE7pJCOFZRb-ZJ8MMA";
    public static final String REST_URL = SUPABASE_URL + "/rest/v1";
    public static final String AUTH_URL = SUPABASE_URL + "/auth/v1";

    private static SupabaseClient instance;
    private final OkHttpClient httpClient;
    private String accessToken = null;

    private SupabaseClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) instance = new SupabaseClient();
        return instance;
    }

    public void setAccessToken(String token) { this.accessToken = token; }
    public String getAccessToken() { return accessToken; }
    public boolean isLoggedIn() { return accessToken != null && !accessToken.isEmpty(); }

    public interface AuthCallback {
        void onSuccess(JSONObject data);
        void onError(String error);
    }

    public void signUp(String email, String password, String username, AuthCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            JSONObject meta = new JSONObject();
            meta.put("username", username);
            meta.put("display_name", username);
            body.put("data", meta);
            authPost("/signup", body.toString(), callback);
        } catch (JSONException e) { callback.onError(e.getMessage()); }
    }

    public void signIn(String email, String password, AuthCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            Request request = new Request.Builder()
                    .url(AUTH_URL + "/token?grant_type=password")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    String rb = response.body().string();
                    try {
                        JSONObject json = new JSONObject(rb);
                        if (response.isSuccessful()) {
                            String token = json.optString("access_token");
                            if (!token.isEmpty()) setAccessToken(token);
                            callback.onSuccess(json);
                        } else {
                            callback.onError(json.optString("error_description", json.optString("msg", "Sign in failed")));
                        }
                    } catch (JSONException ex) { callback.onError("Parse error"); }
                }
            });
        } catch (JSONException e) { callback.onError(e.getMessage()); }
    }

    public void signOut(AuthCallback callback) {
        Request request = new Request.Builder()
                .url(AUTH_URL + "/logout")
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { accessToken = null; callback.onError(e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException { accessToken = null; callback.onSuccess(new JSONObject()); }
        });
    }

    private void authPost(String path, String bodyStr, AuthCallback callback) {
        Request request = new Request.Builder()
                .url(AUTH_URL + path)
                .post(RequestBody.create(bodyStr, MediaType.parse("application/json")))
                .addHeader("apikey", ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String rb = response.body().string();
                try {
                    JSONObject json = new JSONObject(rb);
                    if (response.isSuccessful()) callback.onSuccess(json);
                    else callback.onError(json.optString("error_description", json.optString("msg", "Error")));
                } catch (JSONException ex) { callback.onError("Parse error"); }
            }
        });
    }

    public interface ApiCallback {
        void onSuccess(String responseBody);
        void onError(String error);
    }

    private Request.Builder baseRest(String table, String params) {
        String url = REST_URL + "/" + table + (params != null && !params.isEmpty() ? "?" + params : "");
        Request.Builder b = new Request.Builder().url(url)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation");
        if (accessToken != null) b.addHeader("Authorization", "Bearer " + accessToken);
        return b;
    }

    public void get(String table, String params, ApiCallback callback) { exec(baseRest(table, params).get().build(), callback); }
    public void post(String table, String json, ApiCallback callback) { exec(baseRest(table, null).post(RequestBody.create(json, MediaType.parse("application/json"))).build(), callback); }
    public void patch(String table, String params, String json, ApiCallback callback) { exec(baseRest(table, params).patch(RequestBody.create(json, MediaType.parse("application/json"))).build(), callback); }
    public void delete(String table, String params, ApiCallback callback) { exec(baseRest(table, params).delete().build(), callback); }

    private void exec(Request request, ApiCallback callback) {
        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onError(e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) callback.onSuccess(body);
                else callback.onError("HTTP " + response.code());
            }
        });
    }
  }
