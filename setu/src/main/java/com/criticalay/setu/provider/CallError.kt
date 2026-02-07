package com.criticalay.setu.provider

import android.content.Context
import com.criticalay.setu.R

sealed class CallError {
    object NetworkError : CallError()
    object MicrophonePermissionDenied : CallError()
    object HardwareInUse : CallError()
    data class ProviderError(val message: String, val code: Int) : CallError()
    object None : CallError()

    /**
     * Converts the error into a string the user can actually read.
     */
    fun toHumanReadableString(context: Context): String = when (this) {
        None -> ""
        NetworkError -> context.getString(R.string.setu_error_network)
        MicrophonePermissionDenied -> context.getString(R.string.setu_error_mic_permission)
        HardwareInUse -> context.getString(R.string.setu_error_hardware_in_use)
        is ProviderError -> {
            message.ifBlank { context.getString(R.string.setu_error_provider_default, code) }
        }
    }
}