package com.mapbox.services.android.navigation.ui.v5.instruction;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.mapbox.services.android.navigation.ui.v5.NavigationViewModel;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.alert.AlertView;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;

import timber.log.Timber;

public class NavigationAlertView extends AlertView {

  private static final long THREE_SECOND_DELAY_IN_MILLIS = 3000;
  private NavigationViewModel navigationViewModel;
  private boolean isEnabled = true;

  public NavigationAlertView(Context context) {
    this(context, null);
  }

  public NavigationAlertView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public NavigationAlertView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Sets the NavigationViewModel in the view
   *
   * @param navigationViewModel to set
   */
  public void subscribe(NavigationViewModel navigationViewModel) {
    this.navigationViewModel = navigationViewModel;
  }


  /**
   * Shows this alert view to let user report a problem for the given number of milliseconds
   */
  public void showReportProblem() {
    if (!isEnabled) {
      return;
    }
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        show(getContext().getString(R.string.report_problem),
          NavigationConstants.ALERT_VIEW_PROBLEM_DURATION, true);
      }
    }, THREE_SECOND_DELAY_IN_MILLIS);
  }


  /**
   * This method enables or disables the alert view from being shown during off-route
   * events.
   * <p>
   * Note this will only happen automatically in the context of
   * the {@link com.mapbox.services.android.navigation.ui.v5.NavigationView} or a {@link NavigationViewModel}
   * has been added to the instruction view with
   * {@link InstructionView#subscribe(androidx.lifecycle.LifecycleOwner, NavigationViewModel)}.
   *
   * @param isEnabled true to show during off-route events, false to hide
   */
  public void updateEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  @Nullable
  private FragmentManager obtainSupportFragmentManager() {
    try {
      return ((FragmentActivity) getContext()).getSupportFragmentManager();
    } catch (ClassCastException exception) {
      Timber.e(exception);
      return null;
    }
  }

  private boolean isShowingReportProblem() {
    return getAlertText().equals(getContext().getString(R.string.report_problem));
  }
}
