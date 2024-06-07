package com.mapbox.services.android.navigation.testapp;

import android.app.Application;

import org.maplibre.android.BuildConfig;
import org.maplibre.android.MapLibre;

import timber.log.Timber;

public class NavigationApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }

    MapLibre.getInstance(getApplicationContext());
  }

}
