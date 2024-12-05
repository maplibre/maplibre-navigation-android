package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.android.navigation.v5.models.BannerComponents;

class ExitSignVerifier implements NodeVerifier {

  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return bannerComponents.getType().equals("exit") || bannerComponents.getType().getText().equals("exit-number");
  }
}
