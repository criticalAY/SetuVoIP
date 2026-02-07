package com.criticalay.setu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.criticalay.setu.provider.CallError
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallErrorTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `toHumanReadableString returns empty string for None`() {
        val error = CallError.None
        assertEquals("", error.toHumanReadableString(context))
    }

    @Test
    fun `toHumanReadableString returns correct message for NetworkError`() {
        val error = CallError.NetworkError
        val expected = context.getString(R.string.setu_error_network)
        assertEquals(expected, error.toHumanReadableString(context))
    }

    @Test
    fun `toHumanReadableString returns correct message for MicrophonePermissionDenied`() {
        val error = CallError.MicrophonePermissionDenied
        val expected = context.getString(R.string.setu_error_mic_permission)
        assertEquals(expected, error.toHumanReadableString(context))
    }

    @Test
    fun `toHumanReadableString returns correct message for HardwareInUse`() {
        val error = CallError.HardwareInUse
        val expected = context.getString(R.string.setu_error_hardware_in_use)
        assertEquals(expected, error.toHumanReadableString(context))
    }

    @Test
    fun `ProviderError returns custom message when provided`() {
        val customMsg = "Server Exploded"
        val error = CallError.ProviderError(message = customMsg, code = 500)

        assertEquals(customMsg, error.toHumanReadableString(context))
    }

    @Test
    fun `ProviderError returns default string with code when message is blank`() {
        val errorCode = 404
        val error = CallError.ProviderError(message = " ", code = errorCode)

        val expected = context.getString(R.string.setu_error_provider_default, errorCode)
        assertEquals(expected, error.toHumanReadableString(context))
    }
}