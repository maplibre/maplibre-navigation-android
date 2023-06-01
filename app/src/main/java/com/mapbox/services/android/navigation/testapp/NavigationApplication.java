package com.mapbox.services.android.navigation.testapp;

import android.app.Application;

import com.mapbox.mapboxsdk.BuildConfig;
import com.mapbox.mapboxsdk.Mapbox;

import timber.log.Timber;

public class NavigationApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }

    Mapbox.getInstance(getApplicationContext());
  }

}
