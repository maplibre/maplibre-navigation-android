package com.mapbox.services.android.navigation.ui.v5.instruction;

import com.mapbox.services.android.navigation.v5.models.BannerComponents;

class TextVerifier implements NodeVerifier {
  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return bannerComponents.text() != null && !bannerComponents.text().isEmpty();
  }
}
