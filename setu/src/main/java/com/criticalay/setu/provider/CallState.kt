package com.criticalay.setu.provider

/**
 * Represents the comprehensive state of a telephonic or VoIP call.
 * This data class serves as the single source of truth for the UI and
 * call management logic.
 *
 * @property callerName The display name of the participant.
 * @property callerHandle The unique identifier or phone number of the participant.
 * @property status The current lifecycle stage of the call.
 * @property direction Indicates if the call was initiated locally or received.
 * @property connectTimestamp The epoch time in milliseconds when the call
 * transitioned to the CONNECTED state, used for duration tracking.
 * @property isMuted Whether the local microphone is currently disabled.
 * @property isOnSpeaker Whether the audio output is routed to the device speaker.
 * @property error The specific error state, if any, encountered during the call.
 */
data class CallState(
    val callerName: String,
    val callerHandle: String,
    val status: CallStatus = CallStatus.IDLE,
    val direction: CallDirection = CallDirection.OUTGOING,
    val connectTimestamp: Long? = null,
    val isMuted: Boolean = false,
    val isOnSpeaker: Boolean = false,
    val error: CallError = CallError.None
)
