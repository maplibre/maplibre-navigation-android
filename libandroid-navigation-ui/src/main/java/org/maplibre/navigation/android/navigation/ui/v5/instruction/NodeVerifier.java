package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.core.models.BannerComponents;

interface NodeVerifier {
  boolean isNodeType(BannerComponents bannerComponents);
}
