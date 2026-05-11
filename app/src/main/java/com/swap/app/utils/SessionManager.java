`package com.swap.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SwapSession";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private final SharedPreferences prefs;
    private static SessionManager instance;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) instance = new SessionManager(context);
        return instance;
    }

    public void saveSession(String token, String refreshToken, String userId, String email) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public void saveProfile(String username, String displayName, String avatarUrl) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_AVATAR_URL, avatarUrl)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH_TOKEN, null); }
    public String getUserId() { return prefs.getString(KEY_USER_ID, null); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, null); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getDisplayName() { return prefs.getString(KEY_DISPLAY_NAME, ""); }
    public String getAvatarUrl() { return prefs.getString(KEY_AVATAR_URL, null); }
    public boolean isLoggedIn() { return getToken() != null; }

    public void clearSession() { prefs.edit().clear().apply(); }
}
