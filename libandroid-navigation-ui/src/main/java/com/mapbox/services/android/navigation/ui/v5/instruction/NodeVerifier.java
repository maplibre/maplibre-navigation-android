package com.mapbox.services.android.navigation.ui.v5.instruction;

import com.mapbox.services.android.navigation.v5.models.BannerComponents;

interface NodeVerifier {
  boolean isNodeType(BannerComponents bannerComponents);
}
