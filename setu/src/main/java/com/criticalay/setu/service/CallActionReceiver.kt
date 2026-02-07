package com.criticalay.setu.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.criticalay.setu.core.SetuManager

/**
 * A [BroadcastReceiver] responsible for handling user interactions with the VoIP notification.
 *
 * This receiver acts as the bridge between the Android System UI and the [com.criticalay.setu.provider.VoipProvider].
 * It listens for specific intent actions triggered by notification buttons and
 * forwards those commands to the active call session.
 *
 * This component must be registered in the AndroidManifest.xml for the library
 * actions to function correctly.
 */
class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val provider = SetuManager.getActiveProvider() ?: return

        when (intent?.action) {
            ACTION_HANG_UP -> SetuManager.endCall()

            ACTION_ANSWER -> provider.answerCall()

            ACTION_MUTE -> {
                val isCurrentlyMuted = provider.state.value.isMuted
                provider.toggleMute(!isCurrentlyMuted)
            }

            ACTION_SPEAKER -> {
                val isCurrentlyOnSpeaker = provider.state.value.isOnSpeaker
                provider.toggleSpeaker(!isCurrentlyOnSpeaker)
            }
        }
    }

    companion object {
        /** Intent action triggered to end an active call or decline an incoming one. */
        const val ACTION_HANG_UP = "com.setu.voip.ACTION_HANG_UP"

        /** Intent action triggered to answer an incoming call. */
        const val ACTION_ANSWER = "com.setu.voip.ACTION_ANSWER"

        /** Intent action triggered to toggle the microphone mute state. */
        const val ACTION_MUTE = "com.setu.voip.ACTION_MUTE"

        /** Intent action triggered to toggle the audio output between speaker and earpiece. */
        const val ACTION_SPEAKER = "com.setu.voip.ACTION_SPEAKER"
    }
}