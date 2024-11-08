package org.maplibre.navigation.android.navigation.v5.navigation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import org.maplibre.navigation.android.navigation.v5.models.LegStep;
import org.maplibre.navigation.android.navigation.v5.models.RouteOptions;
import org.maplibre.navigation.android.navigation.R;
import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification;
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress;
import org.maplibre.navigation.android.navigation.v5.utils.DistanceFormatter;
import org.maplibre.navigation.android.navigation.v5.utils.LocaleUtils;
import org.maplibre.navigation.android.navigation.v5.utils.ManeuverUtils;

import java.util.Calendar;

import org.maplibre.navigation.android.navigation.v5.utils.time.TimeFormatter;

/**
 * This is in charge of creating the persistent navigation session notification and updating it.
 */
class MapLibreNavigationNotification implements NavigationNotification {

  private static final int INTENT_FLAGS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
          PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
          PendingIntent.FLAG_UPDATE_CURRENT;

  private NotificationCompat.Builder notificationBuilder;
  private NotificationManager notificationManager;
  private Notification notification;
  private RemoteViews collapsedNotificationRemoteViews;
  private RemoteViews expandedNotificationRemoteViews;
  private MapLibreNavigation mapLibreNavigation;
  private SpannableString currentDistanceText;
  private DistanceFormatter distanceFormatter;
  private String instructionText;
  private int currentManeuverId;
  private boolean isTwentyFourHourFormat;
  private String etaFormat;

  private BroadcastReceiver endNavigationBtnReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(final Context context, final Intent intent) {
      MapLibreNavigationNotification.this.onEndNavigationBtnClick();
    }
  };

  MapLibreNavigationNotification(Context context, MapLibreNavigation mapLibreNavigation) {
    initialize(context, mapLibreNavigation);
  }

  @Override
  public Notification getNotification() {
    return notification;
  }

  @Override
  public int getNotificationId() {
    return NavigationConstants.NAVIGATION_NOTIFICATION_ID;
  }

  @Override
  public void updateNotification(RouteProgress routeProgress) {
    updateNotificationViews(routeProgress);
  }

  @Override
  public void onNavigationStopped(Context context) {
    unregisterReceiver(context);
  }

  private void initialize(Context context, MapLibreNavigation mapLibreNavigation) {
    this.mapLibreNavigation = mapLibreNavigation;
    etaFormat = context.getString(R.string.eta_format);
    initializeDistanceFormatter(context, mapLibreNavigation);
    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    isTwentyFourHourFormat = DateFormat.is24HourFormat(context);
    createNotificationChannel(context);
    buildNotification(context);
    registerReceiver(context);
  }

  private void initializeDistanceFormatter(Context context, MapLibreNavigation mapLibreNavigation) {
    RouteOptions routeOptions = mapLibreNavigation.getRoute().routeOptions();
    LocaleUtils localeUtils = new LocaleUtils();
    String language = localeUtils.inferDeviceLanguage(context);
    String unitType = localeUtils.getUnitTypeForDeviceLocale(context);
    if (routeOptions != null) {
      language = routeOptions.language();
      unitType = routeOptions.voiceUnits();
    }
    MapLibreNavigationOptions mapLibreNavigationOptions = mapLibreNavigation.options();
    distanceFormatter = new DistanceFormatter(context, language, unitType, mapLibreNavigationOptions.roundingIncrement());
  }

  private void createNotificationChannel(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(
        NavigationConstants.NAVIGATION_NOTIFICATION_CHANNEL, context.getString(R.string.channel_name),
        NotificationManager.IMPORTANCE_LOW);
      notificationManager.createNotificationChannel(notificationChannel);
    }
  }

  private void buildNotification(Context context) {
    collapsedNotificationRemoteViews = new RemoteViews(context.getPackageName(),
      R.layout.collapsed_navigation_notification_layout);
    expandedNotificationRemoteViews = new RemoteViews(context.getPackageName(),
      R.layout.expanded_navigation_notification_layout);

    PendingIntent pendingOpenIntent = createPendingOpenIntent(context);
    // Will trigger endNavigationBtnReceiver when clicked
    PendingIntent pendingCloseIntent = createPendingCloseIntent(context);
    expandedNotificationRemoteViews.setOnClickPendingIntent(R.id.endNavigationBtn, pendingCloseIntent);

    // Sets up the top bar notification
    notificationBuilder = new NotificationCompat.Builder(context, NavigationConstants.NAVIGATION_NOTIFICATION_CHANNEL)
      .setContentIntent(pendingOpenIntent)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setSmallIcon(R.drawable.ic_navigation)
      .setCustomContentView(collapsedNotificationRemoteViews)
      .setCustomBigContentView(expandedNotificationRemoteViews)
      .setOngoing(true);

    notification = notificationBuilder.build();
  }

  private PendingIntent createPendingOpenIntent(Context context) {
    PackageManager pm = context.getPackageManager();
    Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
    intent.setPackage(null);
    intent.setAction(OPEN_NAVIGATION_ACTION);
    return PendingIntent.getActivity(context, 0, intent, INTENT_FLAGS);
  }

  private void registerReceiver(Context context) {
    if (context != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(endNavigationBtnReceiver, new IntentFilter(END_NAVIGATION_ACTION), Context.RECEIVER_NOT_EXPORTED);
      } else {
        context.registerReceiver(endNavigationBtnReceiver, new IntentFilter(END_NAVIGATION_ACTION));
      }
    }
  }

  /**
   * With each location update and new routeProgress, the notification is checked and updated if any
   * information has changed.
   *
   * @param routeProgress the latest RouteProgress object
   */
  private void updateNotificationViews(RouteProgress routeProgress) {
    updateInstructionText(routeProgress.currentLegProgress().currentStep());
    updateDistanceText(routeProgress);
    updateArrivalTime(routeProgress);
    LegStep step = routeProgress.currentLegProgress().upComingStep() != null
      ? routeProgress.currentLegProgress().upComingStep()
      : routeProgress.currentLegProgress().currentStep();
    updateManeuverImage(step);

    notificationManager.notify(NavigationConstants.NAVIGATION_NOTIFICATION_ID, notificationBuilder.build());
  }

  private void unregisterReceiver(Context context) {
    if (context != null) {
      context.unregisterReceiver(endNavigationBtnReceiver);
    }
    if (notificationManager != null) {
      notificationManager.cancel(NavigationConstants.NAVIGATION_NOTIFICATION_ID);
    }
  }

  private void updateInstructionText(LegStep step) {
    if (hasInstructions(step) && (instructionText == null || newInstructionText(step))) {
      instructionText = step.bannerInstructions().get(0).primary().text();
      collapsedNotificationRemoteViews.setTextViewText(R.id.notificationInstructionText, instructionText);
      expandedNotificationRemoteViews.setTextViewText(R.id.notificationInstructionText, instructionText);
    }
  }

  private boolean hasInstructions(LegStep step) {
    return step.bannerInstructions() != null && !step.bannerInstructions().isEmpty();
  }

  private boolean newInstructionText(LegStep step) {
    return !instructionText.equals(step.bannerInstructions().get(0).primary().text());
  }

  private void updateDistanceText(RouteProgress routeProgress) {
    if (currentDistanceText == null || newDistanceText(routeProgress)) {
      currentDistanceText = distanceFormatter.formatDistance(
        routeProgress.currentLegProgress().currentStepProgress().distanceRemaining());
      collapsedNotificationRemoteViews.setTextViewText(R.id.notificationDistanceText, currentDistanceText);
      expandedNotificationRemoteViews.setTextViewText(R.id.notificationDistanceText, currentDistanceText);
    }
  }

  private boolean newDistanceText(RouteProgress routeProgress) {
    return currentDistanceText != null
      && !currentDistanceText.toString().equals(distanceFormatter.formatDistance(
      routeProgress.currentLegProgress().currentStepProgress().distanceRemaining()).toString());
  }

  private void updateArrivalTime(RouteProgress routeProgress) {
    MapLibreNavigationOptions options = mapLibreNavigation.options();
    Calendar time = Calendar.getInstance();
    double durationRemaining = routeProgress.durationRemaining();
    int timeFormatType = options.timeFormatType();
    String arrivalTime = TimeFormatter.formatTime(time, durationRemaining, timeFormatType, isTwentyFourHourFormat);
    String formattedArrivalTime = String.format(etaFormat, arrivalTime);
    collapsedNotificationRemoteViews.setTextViewText(R.id.notificationArrivalText, formattedArrivalTime);
    expandedNotificationRemoteViews.setTextViewText(R.id.notificationArrivalText, formattedArrivalTime);
  }

  private void updateManeuverImage(LegStep step) {
    if (newManeuverId(step)) {
      int maneuverResource = ManeuverUtils.getManeuverResource(step);
      currentManeuverId = maneuverResource;
      collapsedNotificationRemoteViews.setImageViewResource(R.id.maneuverImage, maneuverResource);
      expandedNotificationRemoteViews.setImageViewResource(R.id.maneuverImage, maneuverResource);
    }
  }

  private boolean newManeuverId(LegStep step) {
    return currentManeuverId != ManeuverUtils.getManeuverResource(step);
  }

  private PendingIntent createPendingCloseIntent(Context context) {
    Intent endNavigationBtn = new Intent(END_NAVIGATION_ACTION);
    return PendingIntent.getBroadcast(context, 0, endNavigationBtn, INTENT_FLAGS);
  }

  private void onEndNavigationBtnClick() {
    if (mapLibreNavigation != null) {
      mapLibreNavigation.stopNavigation();
    }
  }
}