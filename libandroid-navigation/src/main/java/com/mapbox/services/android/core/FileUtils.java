package com.mapbox.services.android.core;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * File utility class
 */
public final class FileUtils {
    private static final String LOG_TAG = "FileUtils";
    private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 4096;

    private FileUtils() {
    }

    /**
     * Return file from context.getFilesDir()/fileName
     *
     * @param context  application context
     * @param fileName path to the file
     * @return instance of the file object.
     */
    @NonNull
    public static File getFile(@NonNull Context context, @NonNull String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    /**
     * Read from file.
     *
     * @param file valid reference to the file.
     * @return content read from the file.
     */
    @NonNull
    public static String readFromFile(@NonNull File file) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(file);
        Reader inputStreamReader = new InputStreamReader(inputStream);
        StringWriter output = new StringWriter();
        try {
            final char[] buffer = new char[DEFAULT_BUFFER_SIZE_IN_BYTES];
            int count;
            while ((count = inputStreamReader.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        } catch (IOException ioe) {
            Log.w(LOG_TAG, ioe.toString());
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, ioe.toString());
            }
        }
        return output.toString();
    }

    /**
     * Write to file.
     *
     * @param file    valid reference to the file.
     * @param content content to write to the file.
     */
    public static void writeToFile(@NonNull File file, @NonNull String content) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        try {
            writer.write(content);
            writer.flush();
        } catch (IOException ioe) {
            Log.e(LOG_TAG, ioe.toString());
        } finally {
            try {
                writer.close();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, ioe.toString());
            }
        }
    }

    /**
     * Delete file.
     *
     * @param file to delete.
     */
    public static void deleteFile(@NonNull File file) {
        boolean deleted = file.delete();
        if (!deleted) {
            Log.w(LOG_TAG, "Could not delete file: " + file);
        }
    }

    /**
     * Return list of all files in the directory.
     *
     * @param directory target directory on file system
     * @return list of files in the directory or empty list if directory is empty.
     */
    @NonNull
    public static File[] listAllFiles(File directory) {
        if (directory == null) {
            return new File[0];
        }
        File[] files = directory.listFiles();
        return files != null ? files : new File[0];
    }

    /**
     * Delete first n files sorted by property.
     *
     * @param files    list of files to delete.
     * @param sortedBy sorting comparator.
     * @param numFiles number of files from list to delete.
     */
    public static void deleteFirst(@NonNull File[] files, @NonNull Comparator<File> sortedBy, int numFiles) {
        Arrays.sort(files, sortedBy);
        int size = Math.min(files.length, numFiles);
        for (int i = 0; i < size; i++) {
            if (!files[i].delete()) {
                Log.w(LOG_TAG, "Failed to delete file: " + files[i]);
            }
        }
    }

    /**
     * Comparator for ordering files from oldest to newest, based of their modified date.
     */
    public static final class LastModifiedComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            long o1LastModified = o1.lastModified();
            long o2LastModified = o2.lastModified();
            return o1LastModified < o2LastModified ? -1 : (o1LastModified == o2LastModified ? 0 : 1);
        }
    }
}
