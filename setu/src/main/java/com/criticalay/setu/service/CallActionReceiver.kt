package com.criticalay.setu.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.criticalay.setu.core.SetuManager

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
        const val ACTION_HANG_UP = "com.setu.voip.ACTION_HANG_UP"
        const val ACTION_ANSWER = "com.setu.voip.ACTION_ANSWER"
        const val ACTION_MUTE = "com.setu.voip.ACTION_MUTE"
        const val ACTION_SPEAKER = "com.setu.voip.ACTION_SPEAKER"
    }
}