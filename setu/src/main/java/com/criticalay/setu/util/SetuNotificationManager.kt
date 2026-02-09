package com.criticalay.setu.util

import android.app.*
import android.content.*
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.criticalay.setu.R
import com.criticalay.setu.core.SetuManager
import com.criticalay.setu.permission.SetuPermissions
import com.criticalay.setu.provider.CallDirection
import com.criticalay.setu.provider.CallError
import com.criticalay.setu.provider.CallState
import com.criticalay.setu.provider.CallStatus
import com.criticalay.setu.service.CallActionReceiver
import timber.log.Timber


/**
 * Manages the creation and updates of the foreground notification for VoIP calls.
 *
 * This class handles the complexity of Android notification channels, localized strings,
 * and mapping [CallState] transitions to user-visible UI elements.
 *
 * @property context The [Context] used to access system services and resources.
 * @constructor Creates a manager and automatically ensures the notification channel exists.
 */
class SetuNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val config = SetuManager.notificationConfig

    init {
        createNotificationChannel()
    }


    /**
     * Constructs the [Notification] object based on the current [CallState].
     *
     * This method performs the following:
     * 1. Resolves localized status text or error messages.
     * 2. Appends a live duration timer if the call is in the [CallStatus.CONNECTED] state.
     * 3. Dynamically adds actions (Mute, Speaker, Answer, Hang Up) based on [SetuManager.notificationConfig].
     * 4. Configures the intent to return to the app when the notification is tapped.
     *
     * @param state The current snapshot of the call's state (status, direction, errors, etc.).
     * @return A fully configured [Notification] ready to be passed to [android.app.Service.startForeground].
     */
    fun buildNotification(state: CallState): Notification {
        Timber.d("Building notification for state: ${state.status}, Direction: ${state.direction}")

        val baseText = if (state.error != CallError.None) {
            Timber.e("Call error detected: ${state.error}")
            state.error.toHumanReadableString(context)
        } else {
            state.status.toHumanReadableString(context)
        }

        val contentText = if (state.status == CallStatus.CONNECTED && state.connectTimestamp != null) {
            val duration = (System.currentTimeMillis() - state.connectTimestamp) / 1000
            "$baseText • ${CallTimer.formatDuration(duration)}"
        } else {
            baseText
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(config.smallIcon)
            .setContentTitle(state.callerName)
            .setContentText(contentText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(getLaunchIntent())

        if (state.status == CallStatus.RINGING && state.direction == CallDirection.INCOMING) {
            if (SetuPermissions.canShowFullScreenIntent(context)) {
                Timber.i("Setting full screen intent for incoming call")
                builder.setFullScreenIntent(getLaunchIntent(), true)
            } else {
                Timber.w("Full screen intent permission is denied. Call will show as a normal notification.")
            }
        }

        if (config.showMute) builder.addMuteAction(state.isMuted)
        if (config.showSpeaker) builder.addSpeakerAction(state.isOnSpeaker)
        builder.addLifecycleActions(state)

        return builder.build()
    }

    private fun NotificationCompat.Builder.addMuteAction(isMuted: Boolean) {
        val icon = if (isMuted) config.unmuteIcon else config.muteIcon
        val label = context.getString(if (isMuted) config.unmuteLabel else config.muteLabel)
        addAction(icon, label, getPendingBroadcast(CallActionReceiver.ACTION_MUTE))
    }

    private fun NotificationCompat.Builder.addSpeakerAction(isOnSpeaker: Boolean) {
        val label = context.getString(if (isOnSpeaker) config.earpieceLabel else config.speakerLabel)
        addAction(config.speakerIcon, label, getPendingBroadcast(CallActionReceiver.ACTION_SPEAKER))
    }

    private fun NotificationCompat.Builder.addLifecycleActions(state: CallState) {
        if (state.status == CallStatus.RINGING && state.direction == CallDirection.INCOMING) {
            addAction(config.answerIcon, context.getString(config.answerLabel), getPendingBroadcast(CallActionReceiver.ACTION_ANSWER))
            addAction(config.hangUpIcon, context.getString(config.declineLabel), getPendingBroadcast(CallActionReceiver.ACTION_HANG_UP))
        } else {
            addAction(config.hangUpIcon, context.getString(config.hangUpLabel), getPendingBroadcast(CallActionReceiver.ACTION_HANG_UP))
        }
    }

    // --- Internal Helpers ---

    private fun getPendingBroadcast(action: String): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getLaunchIntent(): PendingIntent? {
        if (config.contentIntent != null) return config.contentIntent

        val intent = if (config.targetActivity != null) {
            Intent(context, config.targetActivity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        }

        return PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, context.getString(config.channelName), importance).apply {
                description = "Channel for VoIP incoming and ongoing calls"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "setu_voip_channel"
        const val NOTIFICATION_ID = 999
    }
}