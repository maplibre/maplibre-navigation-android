package org.maplibre.navigation.android.navigation.ui.v5.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.text.SpannableString
import android.text.format.DateFormat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.maplibre.navigation.android.navigation.ui.v5.R
import org.maplibre.navigation.android.navigation.ui.v5.utils.DistanceFormatter
import org.maplibre.navigation.android.navigation.ui.v5.utils.LocaleUtils
import org.maplibre.navigation.android.navigation.ui.v5.utils.ManeuverUtils
import org.maplibre.navigation.android.navigation.ui.v5.utils.time.TimeFormatter
import org.maplibre.navigation.core.models.LegStep
import org.maplibre.navigation.core.models.UnitType
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.routeprogress.RouteProgress
import timber.log.Timber
import java.util.Calendar

/**
 * This is in charge of creating the persistent navigation session notification and updating it.
 */
open class MapLibreNavigationNotification(
    private val context: Context,
    private val mapLibreNavigation: MapLibreNavigation,
    private val maneuverUtils: ManeuverUtils = ManeuverUtils()
) : NavigationNotification {
    private val etaFormat = context.getString(R.string.notification_eta_format)
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val isTwentyFourHourFormat = DateFormat.is24HourFormat(context)
    private val distanceFormatter: DistanceFormatter = initializeDistanceFormatter(context, mapLibreNavigation)
    private val notificationBuilder = NotificationCompat.Builder(context, NAVIGATION_NOTIFICATION_CHANNEL)
    private var notification: Notification? = null
    private var currentDistanceText: SpannableString? = null
    private var instructionText: String? = null
    private var currentManeuverId: Int? = null
    private var formattedArrivalTime: String? = null

    private val pendingOpenIntent: PendingIntent by lazy { createPendingOpenIntent(context) }
    private val pendingCloseIntent: PendingIntent by lazy { createPendingCloseIntent(context) }

    private val endNavigationBtnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            this@MapLibreNavigationNotification.onEndNavigationBtnClick()
        }
    }

    init {
        createNotificationChannel(context)
        buildNotification(context)
        registerReceiver(context)
    }

    override fun getNotification(): Notification {
        return checkNotNull(notification) { "Notification has not been initialized" }
    }

    override fun getNotificationId(): Int = NAVIGATION_NOTIFICATION_ID

    override fun updateNotification(routeProgress: RouteProgress) {
        updateNotificationViews(routeProgress)
    }

    override fun onNavigationStopped(context: Context) {
        unregisterReceiver(context)
    }

    private fun initializeDistanceFormatter(
        context: Context,
        mapLibreNavigation: MapLibreNavigation
    ): DistanceFormatter {
        val routeOptions = mapLibreNavigation.route?.routeOptions
        val localeUtils = LocaleUtils()
        val language: String = routeOptions?.language ?: localeUtils.inferDeviceLanguage(context)
        val unitType: UnitType =
            routeOptions?.voiceUnits ?: localeUtils.getUnitTypeForDeviceLocale(context)

        return DistanceFormatter(
            context,
            language,
            unitType,
            mapLibreNavigation.options.roundingIncrement
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NAVIGATION_NOTIFICATION_CHANNEL,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(context: Context) {
        val collapsedView = createCollapsedView(context)
        val expandedView = createExpandedView(context)

        notificationBuilder
            .setContentIntent(pendingOpenIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.ic_navigation)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setOngoing(true)

        notification = notificationBuilder.build()
    }

    private fun createCollapsedView(context: Context): RemoteViews =
        RemoteViews(context.packageName, R.layout.collapsed_navigation_notification_layout)

    private fun createExpandedView(context: Context): RemoteViews =
        RemoteViews(context.packageName, R.layout.expanded_navigation_notification_layout).apply {
            setOnClickPendingIntent(R.id.endNavigationBtn, pendingCloseIntent)
        }

    private fun createPendingOpenIntent(context: Context): PendingIntent {
        val intent = checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) {
            "Unable to get launch intent for package: ${context.packageName}"
        }
        intent.setPackage(null)
        intent.action = NavigationNotification.OPEN_NAVIGATION_ACTION
        return PendingIntent.getActivity(context, 0, intent, INTENT_FLAGS)
    }

    private fun createPendingCloseIntent(context: Context): PendingIntent {
        val intent = Intent(NavigationNotification.END_NAVIGATION_ACTION)
        return PendingIntent.getBroadcast(context, 0, intent, INTENT_FLAGS)
    }

    private fun registerReceiver(context: Context) {
        ContextCompat.registerReceiver(
            context,
            endNavigationBtnReceiver,
            IntentFilter(NavigationNotification.END_NAVIGATION_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterReceiver(context: Context) {
        try {
            context.unregisterReceiver(endNavigationBtnReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Receiver was not registered")
        }
        notificationManager.cancel(NAVIGATION_NOTIFICATION_ID)
    }

    private fun onEndNavigationBtnClick() {
        mapLibreNavigation.stopNavigation()
    }

    private fun updateNotificationViews(routeProgress: RouteProgress) {
        val currentLegProgress = routeProgress.currentLegProgress
        val currentStep = currentLegProgress.currentStep
        val upcomingStep = currentLegProgress.upComingStep

        updateInstructionText(currentStep)
        updateDistanceText(currentLegProgress.currentStepProgress.distanceRemaining)
        updateFormattedArrivalTime(routeProgress.durationRemaining)
        updateManeuverImage(upcomingStep ?: currentStep)

        // Create fresh RemoteViews to prevent action accumulation
        val collapsedView = createCollapsedView(context)
        val expandedView = createExpandedView(context)
        applyValuesToRemoteViews(collapsedView, expandedView)

        // Update builder with fresh RemoteViews and build notification
        notification = notificationBuilder
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .build()
            .also {
                notificationManager.notify(NAVIGATION_NOTIFICATION_ID, it)
            }
    }

    private fun applyValuesToRemoteViews(
        collapsedView: RemoteViews,
        expandedView: RemoteViews
    ) {
        instructionText?.let { text ->
            collapsedView.setTextViewText(R.id.notificationInstructionText, text)
            expandedView.setTextViewText(R.id.notificationInstructionText, text)
        }

        currentDistanceText?.let { distance ->
            collapsedView.setTextViewText(R.id.notificationDistanceText, distance)
            expandedView.setTextViewText(R.id.notificationDistanceText, distance)
        }

        collapsedView.setTextViewText(R.id.notificationArrivalText, formattedArrivalTime)
        expandedView.setTextViewText(R.id.notificationArrivalText, formattedArrivalTime)

        currentManeuverId?.let { id ->
            collapsedView.setImageViewResource(R.id.maneuverImage, id)
            expandedView.setImageViewResource(R.id.maneuverImage, id)
        }
    }

    private fun updateFormattedArrivalTime(durationRemaining: Double){
        val arrivalTime = TimeFormatter.formatTime(
            Calendar.getInstance(),
            durationRemaining,
            mapLibreNavigation.options.timeFormatType,
            isTwentyFourHourFormat
        )
        formattedArrivalTime = String.format(etaFormat, arrivalTime)
    }

    private fun updateInstructionText(step: LegStep) {
        val instructions = step.bannerInstructions?.firstOrNull() ?: return
        val newText = instructions.primary.text

        if (instructionText != newText) {
            instructionText = newText
        }
    }

    private fun updateDistanceText(distanceRemaining: Double) {
        val newDistanceText = distanceFormatter.formatDistance(distanceRemaining)

        if (currentDistanceText?.toString() != newDistanceText.toString()) {
            currentDistanceText = newDistanceText
        }
    }

    private fun updateManeuverImage(step: LegStep) {
        val newManeuverId = maneuverUtils.getManeuverResource(step)

        if (currentManeuverId != 0) {
            currentManeuverId = newManeuverId
        }
    }

    companion object {
        private val INTENT_FLAGS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT

        /**
         * String channel used to post the navigation notification (custom or default).
         *
         *
         * If &gt; Android O, a notification channel needs to be created to properly post the notification.
         *
         * @since 0.8.0
         */
        const val NAVIGATION_NOTIFICATION_CHANNEL: String = "NAVIGATION_NOTIFICATION_CHANNEL"

        /**
         * Random integer value used for identifying the navigation notification.
         *
         * @since 0.5.0
         */
        const val NAVIGATION_NOTIFICATION_ID: Int = 5678
    }
}
