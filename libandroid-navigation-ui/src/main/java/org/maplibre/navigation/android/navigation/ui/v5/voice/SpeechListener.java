package org.maplibre.navigation.android.navigation.ui.v5.voice;

interface SpeechListener {

  void onStart();

  void onDone();

  void onError(String errorText, SpeechAnnouncement speechAnnouncement);
}
