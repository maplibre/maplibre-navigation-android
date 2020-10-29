package com.flitsmeister.libandroid.core.connectivity;

/**
 * Callback to use with the ConnectivityReceiver
 */

interface ConnectivityListener {

  void onConnectivityChanged(boolean connected);
}
