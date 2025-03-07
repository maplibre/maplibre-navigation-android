package org.maplibre.navigation.android.navigation.ui.v5;

import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import android.view.View;

class SummaryBottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

  private NavigationPresenter presenter;
  private NavigationViewEventDispatcher dispatcher;

  SummaryBottomSheetCallback(NavigationPresenter presenter, NavigationViewEventDispatcher dispatcher) {
    this.presenter = presenter;
    this.dispatcher = dispatcher;
  }

  @Override
  public void onStateChanged(@NonNull View bottomSheet, int newState) {
    dispatcher.onBottomSheetStateChanged(bottomSheet, newState);
    presenter.onSummaryBottomSheetHidden();
  }

  @Override
  public void onSlide(@NonNull View bottomSheet, float slideOffset) {
    // No-op
  }
}
