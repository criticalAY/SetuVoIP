package com.criticalay.setu

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.criticalay.setu.core.SetuManager
import com.criticalay.setu.provider.CallState
import com.criticalay.setu.provider.CallStatus
import com.criticalay.setu.provider.VoipProvider
import com.criticalay.setu.service.VoipService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class VoipServiceTest {

    private lateinit var context: Context
    private val mockProvider = mockk<VoipProvider>(relaxed = true)
    private val callStateFlow = MutableStateFlow(CallState(status = CallStatus.IDLE))

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        mockkObject(SetuManager)
        every { SetuManager.getActiveProvider() } returns mockProvider
        every { mockProvider.state } returns callStateFlow
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `service should start and collect state when provider is active`() = runTest {
        val intent = Intent(context, VoipService::class.java)
        val controller = Robolectric.buildService(VoipService::class.java, intent)

        controller.create().startCommand(0, 0)

        verify { SetuManager.getActiveProvider() }
    }

    @Test
    fun `service should call provider answer when ACTION_ANSWER is received`() {
        val intent = Intent(context, VoipService::class.java).apply {
            action = VoipService.ACTION_ANSWER
        }
        val controller = Robolectric.buildService(VoipService::class.java, intent)

        controller.create().startCommand(0, 0)

        verify { mockProvider.answerCall() }
    }

    @Test
    fun `service should shutdown when state becomes DISCONNECTED`() {
        val controller = Robolectric.buildService(VoipService::class.java)
        val service = controller.create().get()
        val spyService = spyk(service, recordPrivateCalls = true)

        val disconnectedState = CallState(status = CallStatus.DISCONNECTED)

        val method = VoipService::class.java.getDeclaredMethod("handleState", CallState::class.java)
        method.isAccessible = true
        method.invoke(spyService, disconnectedState)

        verify { spyService.stopSelf() }
    }

    @Test
    fun `service should start timer when state is CONNECTED`() {
        val intent = Intent(context, VoipService::class.java)
        val controller = Robolectric.buildService(VoipService::class.java, intent)
        val service = controller.create().get()

        // Push CONNECTED state
        callStateFlow.value = CallState(status = CallStatus.CONNECTED)

        controller.startCommand(0, 0)

        val timerJobField = VoipService::class.java.getDeclaredField("timerJob")
        timerJobField.isAccessible = true
        val timerJob = timerJobField.get(service) as? Job

        assert(timerJob != null)
    }

    @Test
    fun `service should clean up resources on onDestroy`() {
        val controller = Robolectric.buildService(VoipService::class.java)
        val service = controller.create().get()

        controller.startCommand(0, 0)

        controller.destroy()

        val stateJobField = VoipService::class.java.getDeclaredField("stateCollectionJob").apply { isAccessible = true }
        val timerJobField = VoipService::class.java.getDeclaredField("timerJob").apply { isAccessible = true }

        assertNull("State collection job should be null", stateJobField.get(service))
        assertNull("Timer job should be null", timerJobField.get(service))
    }

    @Test
    fun `multiple start commands should not create duplicate state collectors`() {
        val controller = Robolectric.buildService(VoipService::class.java)
        controller.create()

        controller.startCommand(0, 0)
        controller.startCommand(0, 0)
        controller.startCommand(0, 0)

        assertEquals(1, callStateFlow.subscriptionCount.value)
    }
}
