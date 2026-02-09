package com.criticalay.setu.ui

import com.criticalay.setu.provider.CallStatus

/**
 * A generic UI state for the VoIP call screen.
 */
data class SetuCallUiState<T>(
    val contactName: String = "Unknown",
    val avatarUrl: String? = null,
    val statusText: String = "",
    val durationText: String = "00:00",
    val isMuted: Boolean = false,
    val isOnSpeaker: Boolean = false,
    val showMuteButton: Boolean = true,
    val showSpeakerButton: Boolean = true,
    val callStatus: CallStatus = CallStatus.IDLE,
    val extraData: T? = null
)