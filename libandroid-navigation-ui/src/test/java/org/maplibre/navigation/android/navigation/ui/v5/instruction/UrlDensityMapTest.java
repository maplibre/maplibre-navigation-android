package org.maplibre.navigation.android.navigation.ui.v5.instruction;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class UrlDensityMapTest {

  @Test
  public void checksDensityLowReturnsOneXPngUrl() {
    int lowDensityDpi = 120;
    UrlDensityMap urlDensityMap = new UrlDensityMap(lowDensityDpi, 18);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@1x.png", threeXPng);
  }

  @Test
  public void checksDensityMediumReturnsOneXPngUrl() {
    int mediumDensityDpi = 160;
    UrlDensityMap urlDensityMap = new UrlDensityMap(mediumDensityDpi, 14);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@1x.png", threeXPng);
  }

  @Test
  public void checksDensityHighReturnsTwoXPngUrl() {
    int highDensityDpi = 240;
    UrlDensityMap urlDensityMap = new UrlDensityMap(highDensityDpi, 15);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@2x.png", threeXPng);
  }

  @Test
  public void checksDensityXHighReturnsThreeXPngUrl() {
    int xhighDensityDpi = 320;
    UrlDensityMap urlDensityMap = new UrlDensityMap(xhighDensityDpi, 21);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@3x.png", threeXPng);
  }

  @Test
  public void checksAndroidJellyBeanAndDensityXxhighReturnsThreeXPngUrl() {
    int xxhighDensityDpi = 480;
    UrlDensityMap urlDensityMap = new UrlDensityMap(xxhighDensityDpi, 16);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@3x.png", threeXPng);
  }

  @Test
  public void checksAndroidJellyBeanMr2AndDensityXxxhighReturnsFourXPngUrl() {
    int xxxhighDensityDpi = 640;
    UrlDensityMap urlDensityMap = new UrlDensityMap(xxxhighDensityDpi, 18);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@4x.png", threeXPng);
  }

  @Test
  public void checksAndroidKitkatAndFourHundredDensityReturnsThreeXPngUrl() {
    int fourHundredDensityDpi = 400;
    UrlDensityMap urlDensityMap = new UrlDensityMap(fourHundredDensityDpi, 19);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@3x.png", threeXPng);
  }

  @Test
  public void checksAndroidLollipopAndFiveHundredAndSixtyDensityReturnsFourXPngUrl() {
    int fiveHundredAndSixtyDensityDpi = 560;
    UrlDensityMap urlDensityMap = new UrlDensityMap(fiveHundredAndSixtyDensityDpi, 21);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@4x.png", threeXPng);
  }

  @Test
  public void checksAndroidLollipopMr1AndTwoHundredAndEightyDensityReturnsTwoXPngUrl() {
    int twoHundredAndEightyDensityDpi = 280;
    UrlDensityMap urlDensityMap = new UrlDensityMap(twoHundredAndEightyDensityDpi, 22);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@2x.png", threeXPng);
  }

  @Test
  public void checksAndroidMAndThreeHundredAndSixtyDensityReturnsThreeXPngUrl() {
    int threeHundredAndSixtyDensityDpi = 360;
    UrlDensityMap urlDensityMap = new UrlDensityMap(threeHundredAndSixtyDensityDpi, 23);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@3x.png", threeXPng);
  }

  @Test
  public void checksAndroidMAndFourHundredAndTwentyDensityReturnsThreeXPngUrl() {
    int fourHundredAndTwentyDensityDpi = 420;
    UrlDensityMap urlDensityMap = new UrlDensityMap(fourHundredAndTwentyDensityDpi, 23);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@3x.png", threeXPng);
  }

  @Test
  public void checksAndroidNMr1AndTwoHundredAndSixtyDensityReturnsTwoXPngUrl() {
    int twoHundredAndSixtyDensityDpi = 260;
    UrlDensityMap urlDensityMap = new UrlDensityMap(twoHundredAndSixtyDensityDpi, 25);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@2x.png", threeXPng);
  }

  @Test
  public void checksAndroidNMr1AndThreeHundredDensityReturnsTwoXPngUrl() {
    int threeHundredDensityDpi = 300;
    UrlDensityMap urlDensityMap = new UrlDensityMap(threeHundredDensityDpi, 25);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@2x.png", threeXPng);
  }

  @Test
  public void checksAndroidNMr1AndThreeHundredAndFortyDensityReturnsThreeXPngUrl() {
    int threeHundredAndFortyDensityDpi = 340;
    UrlDensityMap urlDensityMap = new UrlDensityMap(threeHundredAndFortyDensityDpi, 25);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@3x.png", threeXPng);
  }

  @Test
  public void checksAndroidPAndFourHundredAndFortyDensityReturnsThreeXPngUrl() {
    int fourHundredAndFortyDensityDpi = 440;
    UrlDensityMap urlDensityMap = new UrlDensityMap(fourHundredAndFortyDensityDpi, 28);
    String anyUrl = "any.url";

    String threeXPng = urlDensityMap.get(anyUrl);

    assertEquals(anyUrl + "@3x.png", threeXPng);
  }
}