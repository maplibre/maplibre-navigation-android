package com.mapbox.services.android.core.crashreporter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Crash report data model
 */
public class CrashReport {
    private static final String TAG = "MapboxCrashReport";
    private static final String CRASH_EVENT = "mobile.crash";
    private final JSONObject content;

    CrashReport(Calendar created) {
        this.content = new JSONObject();
        put("event", CRASH_EVENT);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
        put("created", dateFormat.format(created.getTimeInMillis()));
    }

    CrashReport(@NonNull String json) throws JSONException {
        this.content = new JSONObject(json);
    }

    /**
     * Add key value pair to report
     *
     * @param key   valid non-empty key
     * @param value valid string value or null
     */
    public synchronized void put(@NonNull String key, @Nullable String value) {
        if (value == null) {
            putNull(key);
            return;
        }

        try {
            this.content.put(key, value);
        } catch (JSONException je) {
            Log.e(TAG, "Failed json encode value: " + String.valueOf(value));
        }
    }

    /**
     * Return formatted date string
     *
     * @return date string in "yyyy-MM-dd'T'HH:mm:ss.SSSZ" format
     */
    @NonNull
    public String getDateString() {
        return getString("created");
    }

    /**
     * Return json formatted crash data
     *
     * @return valid json string
     */
    @NonNull
    public String toJson() {
        return this.content.toString();
    }

    @VisibleForTesting
    @NonNull
    String getString(@NonNull String key) {
        return this.content.optString(key);
    }

    private void putNull(@NonNull String key) {
        try {
            this.content.put(key, "null");
        } catch (JSONException je) {
            Log.e(TAG, "Failed json encode null value");
        }
    }
}
