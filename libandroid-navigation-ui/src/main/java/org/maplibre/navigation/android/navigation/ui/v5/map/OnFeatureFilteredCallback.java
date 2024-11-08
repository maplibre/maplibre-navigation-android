package org.maplibre.navigation.android.navigation.ui.v5.map;

import androidx.annotation.NonNull;

import org.maplibre.geojson.Feature;

interface OnFeatureFilteredCallback {
  void onFeatureFiltered(@NonNull Feature feature);
}
