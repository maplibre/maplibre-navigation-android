package com.mapbox.services.android.navigation.v5.utils;

/**
 * Includes common variables used throughout the Mapbox Service modules.
 *
 * @since 3.0.0
 */
public final class Constants {

  /**
   * Base URL for all API calls, not hardcoded to enable testing.
   *
   * @since 1.0.0
   */
  public static final String BASE_API_URL = "https://api.mapbox.com";

  /**
   * The default user variable used for all the Mapbox user names.
   *
   * @since 1.0.0
   */
  public static final String MAPBOX_USER = "mapbox";

  /**
   * Use a precision of 6 decimal places when encoding or decoding a polyline.
   *
   * @since 2.1.0
   */
  public static final int PRECISION_6 = 6;

  /**
   * Use a precision of 5 decimal places when encoding or decoding a polyline.
   *
   * @since 1.0.0
   */
  public static final int PRECISION_5 = 5;

  private Constants() {
    // Empty constructor prevents users from initializing this class
  }
}
