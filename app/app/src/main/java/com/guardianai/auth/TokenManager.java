package com.guardianai.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Keystore-backed secure TokenManager for persisting JWT access tokens securely.
 */
public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "guardian_secure_prefs";
    private static final String KEY_ACCESS_TOKEN = "jwt_access_token";
    private static final String KEY_USER_EMAIL = "user_email";

    private static TokenManager instance;
    private SharedPreferences prefs;

    private TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context.getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context.getApplicationContext(),
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "EncryptedSharedPreferences initialization failed, falling back to standard SharedPreferences: " + e.getMessage());
            prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * For unit testing injection.
     */
    public static synchronized void setTestInstance(TokenManager testInstance) {
        instance = testInstance;
    }

    public void saveToken(String token, String email) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getSavedEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearToken() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_USER_EMAIL)
                .apply();
    }
}
