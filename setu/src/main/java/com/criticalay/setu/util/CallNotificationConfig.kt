package com.criticalay.setu.util

import android.app.PendingIntent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.criticalay.setu.R

/** Configuration class used to customize the appearance and behavior of VoIP notifications. */
data class CallNotificationConfig(
    val showMute: Boolean = true,
    val showSpeaker: Boolean = true,
    val targetActivity: Class<*>? = null,
    val contentIntent: PendingIntent? = null,

    @param:StringRes internal val channelName: Int = R.string.setu_channel_name,
    @param:StringRes internal val muteLabel: Int = R.string.setu_mute,
    @param:StringRes internal val unmuteLabel: Int = R.string.setu_unmute,
    @param:StringRes internal val speakerLabel: Int = R.string.setu_speaker,
    @param:StringRes internal val earpieceLabel: Int = R.string.setu_earpiece,
    @param:StringRes internal val answerLabel: Int = R.string.setu_answer,
    @param:StringRes internal val declineLabel: Int = R.string.setu_decline,
    @param:StringRes internal val hangUpLabel: Int = R.string.setu_hang_up,

    @param:DrawableRes val smallIcon: Int = R.drawable.ic_call,
    @param:DrawableRes val muteIcon: Int = R.drawable.ic_mic,
    @param:DrawableRes val unmuteIcon: Int = R.drawable.ic_mic_off,
    @param:DrawableRes val speakerIcon: Int = R.drawable.ic_mobile_speaker,
    @param:DrawableRes val answerIcon: Int = R.drawable.ic_call,
    @param:DrawableRes val hangUpIcon: Int = R.drawable.ic_call_end,

    val customActions: List<NotificationCompat.Action> = emptyList()
)