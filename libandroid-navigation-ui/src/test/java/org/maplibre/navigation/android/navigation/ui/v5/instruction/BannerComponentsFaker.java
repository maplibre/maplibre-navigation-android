package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import org.maplibre.navigation.android.navigation.v5.models.BannerComponents;

class BannerComponentsFaker {
  static BannerComponents bannerComponents() {
    return bannerComponentsBuilder().build();
  }

  static BannerComponents.Builder bannerComponentsBuilder() {
    return new BannerComponents.Builder(
        "some text",
        BannerComponents.Type.EXIT
    );
  }

  static BannerComponents bannerComponentsWithAbbreviation() {
    return bannerComponentsBuilder()
      .withAbbreviationPriority(1)
      .withAbbreviation("abbreviation text")
      .build();
  }
}
