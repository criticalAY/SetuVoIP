package com.criticalay.setu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.criticalay.setu.core.SetuMain
import com.criticalay.setu.core.SetuManager
import com.criticalay.setu.provider.CallDirection
import com.criticalay.setu.provider.CallState
import com.criticalay.setu.provider.CallStatus
import com.criticalay.setu.provider.VoipProvider
import com.criticalay.setu.util.CallTimer
import com.criticalay.setu.util.SetuNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * The core Foreground Service responsible for maintaining the VoIP call lifecycle.
 *
 * This service ensures the application process remains alive during an active call,
 * manages a persistent notification, and coordinates state updates between the
 * [VoipProvider] and the [SetuNotificationManager].
 */
class VoipService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private var stateCollectionJob: Job? = null
    private lateinit var setuNotificationManager: SetuNotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.i("VoipService created")
        setuNotificationManager = SetuNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Timber.d("onStartCommand received action: $action")

        val provider = SetuManager.getActiveProvider() ?: run {
            Timber.e("No active provider found. Shutting down service.")
            shutdown()
            return START_NOT_STICKY
        }

        when (action) {
            ACTION_HANG_UP -> {
                Timber.i("User action: Hang Up")
                provider.endCall()
            }
            ACTION_ANSWER -> {
                Timber.i("User action: Answer")
                provider.answerCall()
            }
            else -> {
                if (stateCollectionJob == null) {
                    Timber.d("Starting CallState collection flow")
                    stateCollectionJob = serviceScope.launch {
                        provider.state.collect { handleState(it) }
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun handleState(state: CallState) {
        Timber.v("State update received: ${state.status}")

        when (state.status) {
            CallStatus.DISCONNECTING, CallStatus.DISCONNECTED -> {
                Timber.i("Call disconnected/ing. Initiating shutdown.")
                shutdown()
                return
            }

            CallStatus.CONNECTED -> {
                startTimer()
            }

            else -> stopTimer()
        }

        updateForegroundNotification(state)
    }

    private fun updateForegroundNotification(state: CallState) {
        val notification = setuNotificationManager.buildNotification(state)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Timber.d("Starting foreground with Android 14+ types (Mic & Phone)")
                startForeground(
                    SetuNotificationManager.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
            } else {
                startForeground(SetuNotificationManager.NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to start foreground service. Check permissions/manifest.")
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return

        Timber.d("Starting notification duration timer")
        timerJob = serviceScope.launch {
            while (isActive) {
                val provider = SetuManager.getActiveProvider()
                if (provider != null) {
                    val notification = setuNotificationManager.buildNotification(provider.state.value)
                    val manager = getSystemService(NotificationManager::class.java)
                    manager.notify(SetuNotificationManager.NOTIFICATION_ID, notification)
                } else {
                    Timber.w("Timer running but provider is null. Stopping.")
                    stopTimer()
                }
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        if (timerJob != null) {
            Timber.d("Stopping notification duration timer")
            timerJob?.cancel()
            timerJob = null
        }
    }

    private fun shutdown() {
        Timber.i("Service shutdown sequence initiated")
        stopTimer()
        stateCollectionJob?.cancel()
        stateCollectionJob = null
        serviceScope.coroutineContext.cancelChildren()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        Timber.i("VoipService destroyed")
        shutdown()
        super.onDestroy()
    }

    companion object {
        /** Intent action to end the current call. */
        const val ACTION_HANG_UP = "com.setu.voip.ACTION_HANG_UP"

        /** Intent action to answer an incoming call. */
        const val ACTION_ANSWER = "com.setu.voip.ACTION_ANSWER"
    }
}