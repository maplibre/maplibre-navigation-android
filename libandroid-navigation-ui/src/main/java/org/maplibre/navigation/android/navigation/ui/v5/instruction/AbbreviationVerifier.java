package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.android.navigation.v5.models.BannerComponents;
import org.maplibre.navigation.android.navigation.v5.utils.TextUtils;

class AbbreviationVerifier implements NodeVerifier {
  @Override
  public boolean isNodeType(BannerComponents bannerComponents) {
    return hasAbbreviation(bannerComponents);
  }

  private boolean hasAbbreviation(BannerComponents components) {
    return !TextUtils.isEmpty(components.abbreviation());
  }
}
