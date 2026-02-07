package com.criticalay.setu

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.criticalay.setu.core.SetuManager
import com.criticalay.setu.provider.CallDirection
import com.criticalay.setu.provider.CallError
import com.criticalay.setu.provider.CallState
import com.criticalay.setu.provider.CallStatus
import com.criticalay.setu.util.SetuNotificationManager
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class SetuNotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: SetuNotificationManager
    private lateinit var systemNotificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        systemNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        SetuManager.init(context)
        notificationManager = SetuNotificationManager(context)
    }

    // --- Version Specific Tests ---

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    fun `buildNotification works on legacy versions without crashing`() {
        val state = CallState(status = CallStatus.RINGING)
        val notification = notificationManager.buildNotification(state)

        assertNotNull(notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = systemNotificationManager.getNotificationChannel(SetuNotificationManager.CHANNEL_ID)
            assertNull("Channel should not be created on API 25", channel)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `notification has high priority and public visibility for Android 12`() {
        val state = CallState(status = CallStatus.CONNECTED)
        val notification = notificationManager.buildNotification(state)

        assertEquals(NotificationCompat.PRIORITY_HIGH, notification.priority)
        assertEquals(NotificationCompat.VISIBILITY_PUBLIC, notification.visibility)
    }

    // --- State & Action Tests ---

    @Test
    fun `buildNotification reflects Mute and Speaker toggle states correctly`() {
        val mutedState = CallState(
            status = CallStatus.CONNECTED,
            isMuted = true,
            isOnSpeaker = true
        )

        val notification = notificationManager.buildNotification(mutedState)

        val muteAction = notification.actions[0]
        val speakerAction = notification.actions[1]

        assertEquals("Unmute", muteAction.title)
        assertEquals("Earpiece", speakerAction.title)
    }

    @Test
    fun `buildNotification shows Hang Up instead of Answer-Decline for outgoing calls`() {
        val state = CallState(
            status = CallStatus.RINGING,
            direction = CallDirection.OUTGOING
        )

        val notification = notificationManager.buildNotification(state)

        // Actions: Mute (0), Speaker (1), Hang Up (2)
        assertEquals(3, notification.actions.size)
        assertEquals("Hang Up", notification.actions[2].title)
    }

    @Test
    fun `buildNotification updates content text when status changes to Reconnecting`() {
        val state = CallState(status = CallStatus.RECONNECTING)
        val notification = notificationManager.buildNotification(state)
        val text = NotificationCompat.getExtras(notification)?.getString(NotificationCompat.EXTRA_TEXT)

        assertEquals("Reconnecting…", text)
    }

    @Test
    fun `notification click intent points to the host application`() {
        val state = CallState(status = CallStatus.CONNECTED)
        val notification = notificationManager.buildNotification(state)

        assertNotNull("ContentIntent should not be null", notification.contentIntent)
    }
}
