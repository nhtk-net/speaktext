package com.example.speaktext.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.speaktext.models.AppConstants;
import com.example.speaktext.models.UserSessionData;
import com.google.gson.Gson;

public class Common {
    public static void storageSet(Context context, final String KEY, String property) {
        SharedPreferences.Editor editor = context.getSharedPreferences(AppConstants.SHARED_PREFERENCES_KEY, context.MODE_PRIVATE).edit();
        editor.putString(KEY, property);
        editor.commit();
    }

    public static String storageGet(Context context, final String KEY) {
        return context.getSharedPreferences(AppConstants.SHARED_PREFERENCES_KEY, context.MODE_PRIVATE).getString(KEY, null);
    }
    public static UserSessionData getStoredUser(Context context) {
        String dataJson;
        UserSessionData user = null;
        try {
            dataJson = storageGet(context, AppConstants.KEY_USER_SESSION);
            if(dataJson == null)
                return null;
            user = new UserSessionData() ;
            Gson gson = new Gson();
            user = gson.fromJson(dataJson, UserSessionData.class);
        } catch (Exception e) {
            Log.d("[getStoredUser]", "Problem reading stored cookies. fallback with empty cookies " + e.getMessage());
        }
        return user;
    }
    public static void setStoredUser(Context context, UserSessionData user) {
        Gson gson = new Gson();
        String dataJson = gson.toJson(user);
        storageSet(context, AppConstants.KEY_USER_SESSION, dataJson);
    }
    public static void clearStoredUser(Context context) {
        storageSet(context, AppConstants.KEY_USER_SESSION, null);
    }
}
