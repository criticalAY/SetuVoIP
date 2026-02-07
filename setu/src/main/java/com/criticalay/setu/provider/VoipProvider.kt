package com.criticalay.setu.provider

import kotlinx.coroutines.flow.StateFlow

interface VoipProvider {
    val state: StateFlow<CallState>

    fun startCall(handle: String)

    fun answerCall()

    fun rejectCall()

    fun endCall()

    fun toggleMute(muted: Boolean)

    fun toggleSpeaker(enabled: Boolean)

    fun release()
}