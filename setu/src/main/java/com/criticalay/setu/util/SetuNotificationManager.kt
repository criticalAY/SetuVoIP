package com.criticalay.setu.util

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import com.criticalay.setu.R
import com.criticalay.setu.core.SetuManager
import com.criticalay.setu.provider.CallDirection
import com.criticalay.setu.provider.CallError
import com.criticalay.setu.provider.CallState
import com.criticalay.setu.provider.CallStatus
import com.criticalay.setu.service.CallActionReceiver

class SetuNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val config = SetuManager.notificationConfig

    init {
        createNotificationChannel()
    }


    fun buildNotification(state: CallState): Notification {
        val baseText = if (state.error != CallError.None) {
            state.error.toHumanReadableString(context)
        } else {
            state.status.toHumanReadableString(context)
        }

        // Logic for the ticking timer
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
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        return PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, context.getString(config.channelName), NotificationManager.IMPORTANCE_LOW).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "setu_voip_channel"
        const val NOTIFICATION_ID = 999
    }
}