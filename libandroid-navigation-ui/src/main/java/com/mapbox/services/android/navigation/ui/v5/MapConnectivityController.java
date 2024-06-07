package com.mapbox.services.android.navigation.ui.v5;

import org.maplibre.android.MapLibre;

class MapConnectivityController {

  void assign(Boolean state) {
    MapLibre.setConnected(state);
  }
}
