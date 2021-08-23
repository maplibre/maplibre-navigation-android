package com.mapbox.services.android.core.crashreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.mapbox.services.android.core.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mapbox custom exception handler, which catches unhandled fatal exceptions
 * caused by Mapbox classes. This is an attempt to capture mapbox exceptions as reliably
 * as possible with minimal false positives.
 * <p>
 * Note: this handler is not capturing full application's stacktrace!
 */
public class MapboxUncaughtExceptionHanlder implements Thread.UncaughtExceptionHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String MAPBOX_PREF_ENABLE_CRASH_REPORTER = "mapbox.crash.enable";
    public static final String MAPBOX_CRASH_REPORTER_PREFERENCES = "MapboxCrashReporterPrefs";

    private static final String TAG = "MbUncaughtExcHandler";
    private static final String CRASH_FILENAME_FORMAT = "%s/%s.crash";
    private static final int DEFAULT_EXCEPTION_CHAIN_DEPTH = 2;
    private static final int DEFAULT_MAX_REPORTS = 10;

    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private final Context applicationContext;
    private final AtomicBoolean isEnabled = new AtomicBoolean(true);
    private final String mapboxPackage;
    private final String version;

    private int exceptionChainDepth;

    @VisibleForTesting
    MapboxUncaughtExceptionHanlder(@NonNull Context applicationContext,
                                   @NonNull SharedPreferences sharedPreferences,
                                   @NonNull String mapboxPackage,
                                   @NonNull String version,
                                   Thread.UncaughtExceptionHandler defaultExceptionHandler) {
        if (TextUtils.isEmpty(mapboxPackage) || TextUtils.isEmpty(version)) {
            throw new IllegalArgumentException("Invalid package name: " + mapboxPackage + " or version: " + version);
        }
        this.applicationContext = applicationContext;
        this.mapboxPackage = mapboxPackage;
        this.version = version;
        this.exceptionChainDepth = DEFAULT_EXCEPTION_CHAIN_DEPTH;
        this.defaultExceptionHandler = defaultExceptionHandler;
        initializeSharedPreferences(sharedPreferences);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // If we're not enabled or crash is not in Mapbox code
        // then just pass the Exception on to the defaultExceptionHandler.
        List<Throwable> causalChain;
        if (isEnabled.get() && isMapboxCrash(causalChain = getCausalChain(throwable))) {
            try {
                CrashReport report = CrashReportBuilder.setup(applicationContext, mapboxPackage, version)
                        .addExceptionThread(thread)
                        .addCausalChain(causalChain)
                        .build();

                ensureDirectoryWritable(applicationContext, mapboxPackage);

                File file = FileUtils.getFile(applicationContext, getReportFileName(mapboxPackage, report.getDateString()));
                FileUtils.writeToFile(file, report.toJson());
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }

        // Give default exception handler a chance to handle exception
        if (defaultExceptionHandler != null) {
            defaultExceptionHandler.uncaughtException(thread, throwable);
        } else {
            Log.i(TAG, "Default exception handler is null");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!MAPBOX_PREF_ENABLE_CRASH_REPORTER.equals(key)) {
            return;
        }

        try {
            isEnabled.set(sharedPreferences.getBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, false));
        } catch (Exception ex) {
            // In case of a ClassCastException
            Log.e(TAG, ex.toString());
        }
    }

    @VisibleForTesting
    boolean isEnabled() {
        return isEnabled.get();
    }

    /**
     * Set exception chain depth we're interested in to dig into backtrace data.
     *
     * @param depth of exception chain
     */
    @VisibleForTesting
    void setExceptionChainDepth(@IntRange(from = 1, to = 256) int depth) {
        this.exceptionChainDepth = depth;
    }

    @VisibleForTesting
    boolean isMapboxCrash(List<Throwable> throwables) {
        for (Throwable cause : throwables) {
            final StackTraceElement[] stackTraceElements = cause.getStackTrace();
            for (final StackTraceElement element : stackTraceElements) {
                if (isMapboxStackTraceElement(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    @VisibleForTesting
    List<Throwable> getCausalChain(@Nullable Throwable throwable) {
        List<Throwable> causes = new ArrayList<>(4);
        int level = 0;
        while (throwable != null) {
            if (isMidOrLowLevelException(++level)) {
                causes.add(throwable);
            }
            throwable = throwable.getCause();
        }
        return Collections.unmodifiableList(causes);
    }

    private boolean isMapboxStackTraceElement(@NonNull StackTraceElement element) {
        return element.getClassName().startsWith(mapboxPackage);
    }

    private boolean isMidOrLowLevelException(int level) {
        return level >= exceptionChainDepth;
    }

    @VisibleForTesting
    static void ensureDirectoryWritable(@NonNull Context context, @NonNull String dirPath) {
        File directory = FileUtils.getFile(context, dirPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Cleanup directory if we've reached our max limit
        File[] allFiles = FileUtils.listAllFiles(directory);
        if (allFiles.length >= DEFAULT_MAX_REPORTS) {
            FileUtils.deleteFirst(allFiles, new FileUtils.LastModifiedComparator(), DEFAULT_MAX_REPORTS - 1);
        }
    }

    @VisibleForTesting
    @NonNull
    static String getReportFileName(@NonNull String mapboxPackage,
                                    @NonNull String timestamp) {
        return String.format(CRASH_FILENAME_FORMAT, mapboxPackage, timestamp);
    }

    private void initializeSharedPreferences(SharedPreferences sharedPreferences) {
        try {
            isEnabled.set(sharedPreferences.getBoolean(MAPBOX_PREF_ENABLE_CRASH_REPORTER, true));
        } catch (Exception ex) {
            // In case of a ClassCastException
            Log.e(TAG, ex.toString());
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
}
