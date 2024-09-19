package org.maplibre.navigation.android.navigation.ui.v5.map;

import android.os.Parcel;
import android.os.Parcelable;

public class NavigationMapLibreMapInstanceState implements Parcelable {

  private final NavigationMapSettings settings;

  NavigationMapLibreMapInstanceState(NavigationMapSettings settings) {
    this.settings = settings;
  }

  NavigationMapSettings retrieveSettings() {
    return settings;
  }

  private NavigationMapLibreMapInstanceState(Parcel in) {
    settings = in.readParcelable(NavigationMapSettings.class.getClassLoader());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(settings, flags);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<NavigationMapLibreMapInstanceState> CREATOR =
    new Creator<NavigationMapLibreMapInstanceState>() {
      @Override
      public NavigationMapLibreMapInstanceState createFromParcel(Parcel in) {
        return new NavigationMapLibreMapInstanceState(in);
      }

      @Override
      public NavigationMapLibreMapInstanceState[] newArray(int size) {
        return new NavigationMapLibreMapInstanceState[size];
      }
    };
}
