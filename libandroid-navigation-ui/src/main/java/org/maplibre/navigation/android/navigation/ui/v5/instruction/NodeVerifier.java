package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.android.navigation.v5.models.BannerComponents;

interface NodeVerifier {
  boolean isNodeType(BannerComponents bannerComponents);
}
