package com.criticalay.setu.provider

/**
 * Indicates the origin of the call relative to the local device.
 */
enum class CallDirection {
    /** Call originated from a remote party. */
    INCOMING,

    /** Call originated from the local device. */
    OUTGOING
}