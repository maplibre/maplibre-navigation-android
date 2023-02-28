package com.mapbox.services.android.navigation.ui.v5.voice;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.content.Context;

import com.mapbox.services.android.navigation.ui.v5.ConnectivityStatusProvider;

import org.junit.Test;

import java.util.Locale;

public class SpeechPlayerProviderTest {

  @Test
  public void sanity() {
    SpeechPlayerProvider provider = buildSpeechPlayerProvider(true);

    assertNotNull(provider);
  }

  @Test
  public void voiceLanguageNotSupported_returnsAndroidSpeechPlayer() {
    boolean voiceLanguageNotSupported = false;
    SpeechPlayerProvider provider = buildSpeechPlayerProvider(voiceLanguageNotSupported);

    SpeechPlayer speechPlayer = provider.retrieveSpeechPlayer();

    assertTrue(speechPlayer instanceof AndroidSpeechPlayer);
  }

  @Test
  public void retrieveAndroidSpeechPlayer_alwaysReturnsAndroidSpeechPlayer() {
    SpeechPlayerProvider provider = buildSpeechPlayerProvider(true);

    AndroidSpeechPlayer speechPlayer = provider.retrieveAndroidSpeechPlayer();

    assertNotNull(speechPlayer);
  }

  private SpeechPlayerProvider buildSpeechPlayerProvider(boolean voiceLanguageSupported) {
    Context context = mock(Context.class);
    String language = Locale.US.getLanguage();
    ConnectivityStatusProvider connectivityStatus = mock(ConnectivityStatusProvider.class);
    return new SpeechPlayerProvider(context, language, voiceLanguageSupported, connectivityStatus);
  }
}
