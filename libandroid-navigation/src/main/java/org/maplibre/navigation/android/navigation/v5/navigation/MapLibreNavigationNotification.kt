package org.maplibre.navigation.android.navigation.v5.navigation

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
import org.maplibre.navigation.android.navigation.R
import org.maplibre.navigation.android.navigation.v5.models.LegStep
import org.maplibre.navigation.android.navigation.v5.navigation.notification.NavigationNotification
import org.maplibre.navigation.android.navigation.v5.routeprogress.RouteProgress
import org.maplibre.navigation.android.navigation.v5.utils.DistanceFormatter
import org.maplibre.navigation.android.navigation.v5.utils.LocaleUtils
import org.maplibre.navigation.android.navigation.v5.utils.ManeuverUtils.getManeuverResource
import org.maplibre.navigation.android.navigation.v5.utils.time.TimeFormatter
import java.util.Calendar

/**
 * This is in charge of creating the persistent navigation session notification and updating it.
 */
//TODO: fabi755
internal class MapLibreNavigationNotification(
    context: Context,
    mapLibreNavigation: MapLibreNavigation
) :
    NavigationNotification {
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
    private var notification: Notification? = null
    private var collapsedNotificationRemoteViews: RemoteViews? = null
    private var expandedNotificationRemoteViews: RemoteViews? = null
    private var mapLibreNavigation: MapLibreNavigation? = null
    private var currentDistanceText: SpannableString? = null
    private var distanceFormatter: DistanceFormatter? = null
    private var instructionText: String? = null
    private var currentManeuverId = 0
    private var isTwentyFourHourFormat = false
    private var etaFormat: String? = null

    private val endNavigationBtnReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            this@MapLibreNavigationNotification.onEndNavigationBtnClick()
        }
    }

    init {
        initialize(context, mapLibreNavigation)
    }

    override fun getNotification(): Notification {
        return notification!!
    }

    override fun getNotificationId(): Int {
        return NavigationConstants.NAVIGATION_NOTIFICATION_ID
    }

    override fun updateNotification(routeProgress: RouteProgress) {
        updateNotificationViews(routeProgress)
    }

    override fun onNavigationStopped(context: Context) {
        unregisterReceiver(context)
    }

    private fun initialize(context: Context, mapLibreNavigation: MapLibreNavigation) {
        this.mapLibreNavigation = mapLibreNavigation
        etaFormat = context.getString(R.string.eta_format)
        initializeDistanceFormatter(context, mapLibreNavigation)
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isTwentyFourHourFormat = DateFormat.is24HourFormat(context)
        createNotificationChannel(context)
        buildNotification(context)
        registerReceiver(context)
    }

    private fun initializeDistanceFormatter(
        context: Context,
        mapLibreNavigation: MapLibreNavigation
    ) {
        val routeOptions = mapLibreNavigation.route!!.routeOptions
        val localeUtils = LocaleUtils()
        val language: String = routeOptions?.language ?: localeUtils.inferDeviceLanguage(context)
        val unitType: String = routeOptions?.voiceUnits ?: localeUtils.getUnitTypeForDeviceLocale(context)

        distanceFormatter = DistanceFormatter(
            context,
            language,
            unitType,
            mapLibreNavigation.options.roundingIncrement
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NavigationConstants.NAVIGATION_NOTIFICATION_CHANNEL,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildNotification(context: Context) {
        collapsedNotificationRemoteViews = RemoteViews(
            context.packageName,
            R.layout.collapsed_navigation_notification_layout
        )
        expandedNotificationRemoteViews = RemoteViews(
            context.packageName,
            R.layout.expanded_navigation_notification_layout
        )

        val pendingOpenIntent = createPendingOpenIntent(context)
        // Will trigger endNavigationBtnReceiver when clicked
        val pendingCloseIntent = createPendingCloseIntent(context)
        expandedNotificationRemoteViews!!.setOnClickPendingIntent(
            R.id.endNavigationBtn,
            pendingCloseIntent
        )

        // Sets up the top bar notification
        notificationBuilder =
            NotificationCompat.Builder(context, NavigationConstants.NAVIGATION_NOTIFICATION_CHANNEL)
                .setContentIntent(pendingOpenIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_navigation)
                .setCustomContentView(collapsedNotificationRemoteViews)
                .setCustomBigContentView(expandedNotificationRemoteViews)
                .setOngoing(true)

        notification = notificationBuilder!!.build()
    }

    private fun createPendingOpenIntent(context: Context): PendingIntent {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        intent!!.setPackage(null)
        intent.setAction(NavigationNotification.OPEN_NAVIGATION_ACTION)
        return PendingIntent.getActivity(context, 0, intent, INTENT_FLAGS)
    }

    private fun registerReceiver(context: Context) {
        ContextCompat.registerReceiver(
            context,
            endNavigationBtnReceiver,
            IntentFilter(NavigationNotification.END_NAVIGATION_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    /**
     * With each location update and new routeProgress, the notification is checked and updated if any
     * information has changed.
     *
     * @param routeProgress the latest RouteProgress object
     */
    private fun updateNotificationViews(routeProgress: RouteProgress) {
        updateInstructionText(routeProgress.currentLegProgress.currentStep)
        updateDistanceText(routeProgress)
        updateArrivalTime(routeProgress)
        val step = if (routeProgress.currentLegProgress.upComingStep != null)
            routeProgress.currentLegProgress.upComingStep
        else
            routeProgress.currentLegProgress.currentStep
        updateManeuverImage(step!!)

        notificationManager!!.notify(
            NavigationConstants.NAVIGATION_NOTIFICATION_ID,
            notificationBuilder!!.build()
        )
    }

    private fun unregisterReceiver(context: Context?) {
        context?.unregisterReceiver(endNavigationBtnReceiver)
        if (notificationManager != null) {
            notificationManager!!.cancel(NavigationConstants.NAVIGATION_NOTIFICATION_ID)
        }
    }

    private fun updateInstructionText(step: LegStep) {
        if (hasInstructions(step) && (instructionText == null || newInstructionText(step))) {
            instructionText = step.bannerInstructions!![0].primary.text
            collapsedNotificationRemoteViews!!.setTextViewText(
                R.id.notificationInstructionText,
                instructionText
            )
            expandedNotificationRemoteViews!!.setTextViewText(
                R.id.notificationInstructionText,
                instructionText
            )
        }
    }

    private fun hasInstructions(step: LegStep): Boolean {
        return !step.bannerInstructions.isNullOrEmpty()
    }

    private fun newInstructionText(step: LegStep): Boolean {
        return instructionText != step.bannerInstructions!![0].primary.text
    }

    private fun updateDistanceText(routeProgress: RouteProgress) {
        if (currentDistanceText == null || newDistanceText(routeProgress)) {
            currentDistanceText = distanceFormatter!!.formatDistance(
                routeProgress.currentLegProgress.currentStepProgress.distanceRemaining
            )
            collapsedNotificationRemoteViews!!.setTextViewText(
                R.id.notificationDistanceText,
                currentDistanceText
            )
            expandedNotificationRemoteViews!!.setTextViewText(
                R.id.notificationDistanceText,
                currentDistanceText
            )
        }
    }

    private fun newDistanceText(routeProgress: RouteProgress): Boolean {
        return currentDistanceText != null && currentDistanceText.toString() != distanceFormatter!!.formatDistance(
            routeProgress.currentLegProgress.currentStepProgress.distanceRemaining
        ).toString()
    }

    private fun updateArrivalTime(routeProgress: RouteProgress) {
        val options = mapLibreNavigation!!.options
        val time = Calendar.getInstance()
        val durationRemaining = routeProgress.durationRemaining
        val timeFormatType = options.timeFormatType
        val arrivalTime = TimeFormatter.formatTime(
            time,
            durationRemaining,
            timeFormatType,
            isTwentyFourHourFormat
        )
        val formattedArrivalTime = String.format(etaFormat!!, arrivalTime)
        collapsedNotificationRemoteViews!!.setTextViewText(
            R.id.notificationArrivalText,
            formattedArrivalTime
        )
        expandedNotificationRemoteViews!!.setTextViewText(
            R.id.notificationArrivalText,
            formattedArrivalTime
        )
    }

    private fun updateManeuverImage(step: LegStep) {
        if (newManeuverId(step)) {
            val maneuverResource = getManeuverResource(step)
            currentManeuverId = maneuverResource
            collapsedNotificationRemoteViews!!.setImageViewResource(
                R.id.maneuverImage,
                maneuverResource
            )
            expandedNotificationRemoteViews!!.setImageViewResource(
                R.id.maneuverImage,
                maneuverResource
            )
        }
    }

    private fun newManeuverId(step: LegStep): Boolean {
        return currentManeuverId != getManeuverResource(step)
    }

    private fun createPendingCloseIntent(context: Context): PendingIntent {
        val endNavigationBtn = Intent(NavigationNotification.END_NAVIGATION_ACTION)
        return PendingIntent.getBroadcast(context, 0, endNavigationBtn, INTENT_FLAGS)
    }

    private fun onEndNavigationBtnClick() {
        if (mapLibreNavigation != null) {
            mapLibreNavigation!!.stopNavigation()
        }
    }

    companion object {
        private val INTENT_FLAGS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
    }
}