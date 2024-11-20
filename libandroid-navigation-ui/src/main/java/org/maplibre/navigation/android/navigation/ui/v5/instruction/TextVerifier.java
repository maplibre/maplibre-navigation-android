package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.android.navigation.v5.models.BannerComponents;

class TextVerifier implements NodeVerifier {
  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return bannerComponents.text() != null && !bannerComponents.text().isEmpty();
  }
}
