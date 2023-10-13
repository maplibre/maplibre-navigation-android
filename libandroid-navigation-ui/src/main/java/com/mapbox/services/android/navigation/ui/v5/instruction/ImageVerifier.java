package com.mapbox.services.android.navigation.ui.v5.instruction;

import android.text.TextUtils;

import com.mapbox.services.android.navigation.v5.models.BannerComponents;

class ImageVerifier implements NodeVerifier {

  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return hasImageUrl(bannerComponents);
  }

  boolean hasImageUrl(BannerComponents components) {
    return !TextUtils.isEmpty(components.imageBaseUrl());
  }
}
