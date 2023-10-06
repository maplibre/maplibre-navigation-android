package com.mapbox.services.android.navigation.ui.v5.voice;

import android.content.Context;
import android.media.AudioManager;
import androidx.annotation.NonNull;

import com.mapbox.services.android.navigation.v5.models.DirectionsRoute;
import com.mapbox.services.android.navigation.ui.v5.ConnectivityStatusProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Given to the constructor of {@link NavigationSpeechPlayer}, this class decides which
 * {@link SpeechPlayer} should be used based on voice language compatibility.
 * <p>
 * If the given {@link DirectionsRoute#voiceLanguage()} is not <tt>null</tt>, this means the language is
 * supported by the Mapbox Voice API, which can parse SSML.  The boolean <tt>voiceLanguageSupported</tt> should
 * be try in this case.
 * <p>
 * If false, an instance of {@link MapboxSpeechPlayer} will never be provided to the {@link NavigationSpeechPlayer}.
 * The SDK will default to the {@link AndroidSpeechPlayer} powered by {@link android.speech.tts.TextToSpeech}.
 *
 * @since 0.16.0
 */
public class SpeechPlayerProvider {

  private static final int FIRST_PLAYER = 0;

  private AndroidSpeechPlayer androidSpeechPlayer;
  private List<SpeechPlayer> speechPlayers = new ArrayList<>(2);
  private ConnectivityStatusProvider connectivityStatus;

  /**
   * Constructed when creating an instance of {@link NavigationSpeechPlayer}.
   *
   * @param context                for the initialization of the speech players
   * @param language               to be used
   * @param voiceLanguageSupported true if <tt>voiceLanguage</tt> is not null, false otherwise
   */
  public SpeechPlayerProvider(@NonNull Context context, String language,
                              boolean voiceLanguageSupported) {
    initialize(context, language, voiceLanguageSupported);
  }

  // Package private (no modifier) for testing purposes
  SpeechPlayerProvider(@NonNull Context context, String language,
                       boolean voiceLanguageSupported,
                       ConnectivityStatusProvider connectivityStatus) {
    this(context, language, voiceLanguageSupported);
    this.connectivityStatus = connectivityStatus;
  }

  SpeechPlayer retrieveSpeechPlayer() {
    return androidSpeechPlayer;
  }

  AndroidSpeechPlayer retrieveAndroidSpeechPlayer() {
    return androidSpeechPlayer;
  }

  void setMuted(boolean isMuted) {
    for (SpeechPlayer player : speechPlayers) {
      player.setMuted(isMuted);
    }
  }

  void onOffRoute() {
    for (SpeechPlayer player : speechPlayers) {
      player.onOffRoute();
    }
  }

  void onDestroy() {
    for (SpeechPlayer player : speechPlayers) {
      player.onDestroy();
    }
  }

  private void initialize(@NonNull Context context, String language,
                          boolean voiceLanguageSupported) {
    AudioFocusDelegateProvider provider = buildAudioFocusDelegateProvider(context);
    SpeechAudioFocusManager audioFocusManager = new SpeechAudioFocusManager(provider);
    SpeechListener speechListener = new NavigationSpeechListener(this, audioFocusManager);
    initializeAndroidSpeechPlayer(context, language, speechListener);
    connectivityStatus = new ConnectivityStatusProvider(context);
  }

  private AudioFocusDelegateProvider buildAudioFocusDelegateProvider(Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    return new AudioFocusDelegateProvider(audioManager);
  }


  private void initializeAndroidSpeechPlayer(Context context, String language,
                                             SpeechListener listener) {
    androidSpeechPlayer = new AndroidSpeechPlayer(context, language, listener);
    speechPlayers.add(androidSpeechPlayer);
  }
}
