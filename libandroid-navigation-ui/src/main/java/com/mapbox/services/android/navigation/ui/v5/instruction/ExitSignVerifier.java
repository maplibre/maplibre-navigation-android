package com.mapbox.services.android.navigation.ui.v5.instruction;

import com.mapbox.services.android.navigation.v5.models.BannerComponents;

class ExitSignVerifier implements NodeVerifier {

  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return bannerComponents.type().equals("exit") || bannerComponents.type().equals("exit-number");
  }
}
