package com.mapbox.services.android.navigation.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Random;

import timber.log.Timber;

public class Utils {

  /**
   * Demonstrates converting any Drawable to an Icon, for use as a marker icon.
   */
  public static Icon drawableToIcon(@NonNull Context context, @DrawableRes int id) {
    Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
    Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
      vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    vectorDrawable.draw(canvas);
    return IconFactory.getInstance(context).fromBitmap(bitmap);
  }

  public static LatLng getRandomLatLng(double[] bbox) {
    Random random = new Random();

    double randomLat = bbox[1] + (bbox[3] - bbox[1]) * random.nextDouble();
    double randomLon = bbox[0] + (bbox[2] - bbox[0]) * random.nextDouble();

    LatLng latLng = new LatLng(randomLat, randomLon);
    Timber.d("getRandomLatLng: %s", latLng.toString());
    return latLng;
  }
}
