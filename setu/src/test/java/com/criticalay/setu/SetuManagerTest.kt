package com.criticalay.setu

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.criticalay.setu.core.SetuManager
import com.criticalay.setu.provider.VoipProvider
import com.criticalay.setu.service.VoipService
import com.criticalay.setu.util.CallNotificationConfig
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class SetuManagerTest {

    private lateinit var context: Context
    private val mockProvider = mockk<VoipProvider>(relaxed = true)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        val field = SetuManager::class.java.getDeclaredField("appContext")
        field.isAccessible = true
        field.set(SetuManager, null)

        SetuManager.setActiveProvider(null)
    }

    @Test
    fun `startCall should store provider and notify system`() {
        SetuManager.init(context)

        val config = CallNotificationConfig()

        SetuManager.startCall(mockProvider, config)

        Assert.assertEquals(mockProvider, SetuManager.getActiveProvider())

        Assert.assertEquals(config, SetuManager.notificationConfig)

        val shadowApp = Shadows.shadowOf(context as Application)
        val intent = shadowApp.nextStartedService
        Assert.assertNotNull(intent)
        Assert.assertEquals(VoipService::class.java.name, intent.component?.className)
    }

    @Test
    fun `endCall should clear reference and stop provider`() {
        SetuManager.setActiveProvider(mockProvider)

        SetuManager.endCall()

        verify { mockProvider.endCall() }
        Assert.assertNull(SetuManager.getActiveProvider())
    }

    @Test(expected = IllegalStateException::class)
    fun `startCall should crash if init was never called`() {
        SetuManager.startCall(mockProvider)
    }

    @Test
    fun `reset should clear all internal state`() {
        val mockProvider = mockk<VoipProvider>(relaxed = true)
        SetuManager.init(context)
        SetuManager.setActiveProvider(mockProvider)

        SetuManager.reset()

        Assert.assertNull("Provider should be null after reset", SetuManager.getActiveProvider())


        // We check this by verifying that startCall throws the IllegalStateException again
        Assert.assertThrows(IllegalStateException::class.java) {
            SetuManager.startCall(mockProvider)
        }

        val defaultConfig = CallNotificationConfig()
        Assert.assertNotNull(SetuManager.notificationConfig)
        Assert.assertEquals(defaultConfig, SetuManager.notificationConfig)
    }
}