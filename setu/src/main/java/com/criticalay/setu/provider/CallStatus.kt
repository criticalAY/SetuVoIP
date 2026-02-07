package com.criticalay.setu.provider

import android.content.Context
import com.criticalay.setu.R


/**
 * Defines the various stages of a call's lifecycle.
 */
enum class CallStatus {
    /** No active call session exists. */
    IDLE,

    /** Requesting the service provider to initiate the call. */
    INITIATING,

    /** An incoming or outgoing call is currently alerting the user. */
    RINGING,

    /** The call has been accepted but the media stream is not yet established. */
    CONNECTING,

    /** The call session is active and the media stream is live. */
    CONNECTED,

    /** Signal loss detected; the system is attempting to restore the media stream. */
    RECONNECTING,

    /** Disconnection has been triggered and is awaiting cleanup by the provider. */
    DISCONNECTING,

    /** The session has ended. This is the final state before returning to IDLE. */
    DISCONNECTED;

    fun toHumanReadableString(context: Context): String = when (this) {
        RINGING -> context.getString(R.string.setu_status_ringing)
        CONNECTING -> context.getString(R.string.setu_status_connecting)
        CONNECTED -> context.getString(R.string.setu_status_ongoing)
        RECONNECTING -> context.getString(R.string.setu_status_reconnecting)
        else -> ""
    }
}