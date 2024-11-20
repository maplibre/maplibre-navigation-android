package org.maplibre.navigation.android.core.connectivity;

/**
 * Callback to use with the ConnectivityReceiver
 */

interface ConnectivityListener {

  void onConnectivityChanged(boolean connected);
}
