package com.criticalay.setu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

//
//class VoipService : Service() {
//
//    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
//    private var timerJob: Job? = null
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val provider = SetuManager.getActiveProvider()
//
//        if (provider == null) {
//            stopSelf()
//            return START_NOT_STICKY
//        }
//
//        // Start observing call state
//        serviceScope.launch {
//            provider.state.collect { state ->
//                handleStateChange(state)
//            }
//        }
//
//        return START_STICKY
//    }
//
//    private fun handleStateChange(state: CallState) {
//        when (state.status) {
//            CallStatus.CONNECTED -> {
//                startTimer() // Start ticking the duration
//                showForegroundNotification(state)
//            }
//            CallStatus.DISCONNECTED -> {
//                stopTimer()
//                stopForeground(STOP_FOREGROUND_REMOVE)
//                stopSelf()
//            }
//            else -> showForegroundNotification(state)
//        }
//    }
//
//    private fun startTimer() {
//        if (timerJob?.isActive == true) return
//        timerJob = serviceScope.launch {
//            while (isActive) {
//                delay(1000)
//                // We refresh the notification every second to show the timer
//                val currentState = SetuManager.getActiveProvider()?.state?.value
//                currentState?.let { showForegroundNotification(it) }
//            }
//        }
//    }
//
//    private fun showForegroundNotification(state: CallState) {
//        // We will create this helper next
//        val notification = SetuNotificationHelper.createNotification(this, state)
//
//        // Android 14+ requires specific FGS types
//        startForeground(
//            SetuNotificationHelper.NOTIFICATION_ID,
//            notification
//        )
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        serviceScope.cancel()
//    }
//}
//
//class VoipService : Service() {
//
//    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//    private var timerJob: Job? = null
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    override fun onCreate() {
//        super.onCreate()
//        ensureNotificationChannel()
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val provider = SetuManager.getActiveProvider() ?: run {
//            stopSelf()
//            return START_NOT_STICKY
//        }
//
//        when (intent?.action) {
//            ACTION_HANG_UP -> provider.endCall()
//            ACTION_ANSWER -> provider.answerCall()
//            else -> observeProvider(provider)
//        }
//
//        return START_STICKY
//    }
//
//    private fun observeProvider(provider: VoipProvider) {
//        serviceScope.launch {
//            provider.state.collect { state ->
//                handleState(state)
//            }
//        }
//    }
//
//    private fun handleState(state: CallState) {
//        when (state.status) {
//            CallStatus.RINGING -> {
//                // If it's an incoming call, we show "Answer" and "Decline"
//                val label = if (state.direction == CallDirection.INCOMING) "Incoming Call" else "Dialing..."
//                updateNotification(state, label)
//            }
//            CallStatus.CONNECTING -> {
//                updateNotification(state, "Connecting...")
//            }
//            CallStatus.CONNECTED -> {
//                // Fixed: Use CONNECTED from your enum
//                startTimer(state.connectTimestamp ?: System.currentTimeMillis())
//                updateNotification(state, "Call in Progress")
//            }
//            CallStatus.RECONNECTING -> {
//                updateNotification(state, "Reconnecting...")
//            }
//            CallStatus.DISCONNECTING, CallStatus.DISCONNECTED -> {
//                shutdown()
//            }
//            CallStatus.IDLE -> {
//                // No action needed or shutdown if service is running
//            }
//            else -> updateNotification(state, "Status: ${state.status}")
//        }
//    }
//
//    private fun startTimer(startTime: Long) {
//        if (timerJob != null) return
//        timerJob = serviceScope.launch {
//            while (isActive) {
//                val duration = (System.currentTimeMillis() - startTime) / 1000
//                val formattedTime = formatDuration(duration)
//
//                SetuManager.getActiveProvider()?.state?.value?.let {
//                    updateNotification(it, "Call in Progress • $formattedTime")
//                }
//                delay(1000)
//            }
//        }
//    }
//
//    private fun updateNotification(state: CallState, contentText: String) {
//        val channelId = CHANNEL_ID
//
//        // Hangup Intent
//        val hangupIntent = Intent(this, VoipService::class.java).apply { action = ACTION_HANG_UP }
//        val hangupPendingIntent = PendingIntent.getService(this, 0, hangupIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        val builder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(android.R.drawable.ic_menu_call)
//            .setContentTitle(state.callerName)
//            .setContentText(contentText)
//            .setOngoing(true)
//            .setCategory(NotificationCompat.CATEGORY_CALL)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
//            .setContentIntent(getLaunchIntent())
//
//        // Logic for buttons: If Ringing & Incoming, show Answer. Otherwise show Hang Up.
//        if (state.status == CallStatus.RINGING && state.direction == CallDirection.INCOMING) {
//            val answerIntent = Intent(this, VoipService::class.java).apply { action = ACTION_ANSWER }
//            val answerPendingIntent = PendingIntent.getService(this, 1, answerIntent, PendingIntent.FLAG_IMMUTABLE)
//
//            builder.addAction(android.R.drawable.ic_menu_call, "Answer", answerPendingIntent)
//            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", hangupPendingIntent)
//        } else {
//            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Hang Up", hangupPendingIntent)
//        }
//
//        startForeground(NOTIFICATION_ID, builder.build())
//    }
//
//    private fun formatDuration(seconds: Long): String {
//        val hrs = seconds / 3600
//        val mins = (seconds % 3600) / 60
//        val secs = seconds % 60
//        return if (hrs > 0) String.format("%02d:%02d:%02d", hrs, mins, secs)
//        else String.format("%02d:%02d", mins, secs)
//    }
//
//    private fun getLaunchIntent(): PendingIntent? {
//        val intent = packageManager.getLaunchIntentForPackage(packageName)
//        return PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_IMMUTABLE)
//    }
//
//    private fun ensureNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(CHANNEL_ID, "Active Calls", NotificationManager.IMPORTANCE_LOW).apply {
//                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//            }
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    private fun shutdown() {
//        timerJob?.cancel()
//        timerJob = null
//        serviceScope.cancel()
//        stopForeground(STOP_FOREGROUND_REMOVE)
//        stopSelf()
//    }
//
//    override fun onDestroy() {
//        shutdown()
//        super.onDestroy()
//    }
//
//    companion object {
//        const val CHANNEL_ID = "setu_voip_channel"
//        const val NOTIFICATION_ID = 999
//        const val ACTION_HANG_UP = "com.setu.voip.ACTION_HANG_UP"
//        const val ACTION_ANSWER = "com.setu.voip.ACTION_ANSWER"
//    }
//}


class VoipService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private lateinit var setuNotificationManager: SetuNotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setuNotificationManager = SetuNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val provider = SetuManager.getActiveProvider() ?: run {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_HANG_UP -> provider.endCall()
            ACTION_ANSWER -> provider.answerCall()
            else -> {
                serviceScope.launch {
                    provider.state.collect { handleState(it) }
                }
            }
        }
        return START_STICKY
    }

    private fun handleState(state: CallState) {
        val contentText = when (state.status) {
            CallStatus.RINGING -> if (state.direction == CallDirection.INCOMING) "Incoming Call" else "Dialing..."
            CallStatus.CONNECTING -> "Connecting..."
            CallStatus.RECONNECTING -> "Reconnecting..."
            CallStatus.CONNECTED -> {
                startTimer(state.connectTimestamp ?: System.currentTimeMillis())
                "Call in Progress"
            }
            CallStatus.DISCONNECTING, CallStatus.DISCONNECTED -> {
                shutdown()
                return
            }
            else -> "Status: ${state.status}"
        }

        val notification = setuNotificationManager.buildNotification(state, contentText)
        startForeground(SetuNotificationManager.NOTIFICATION_ID, notification)
    }

    private fun startTimer(startTime: Long) {
        if (timerJob != null) return
        timerJob = serviceScope.launch {
            while (isActive) {
                val duration = (System.currentTimeMillis() - startTime) / 1000
                val formattedTime = CallTimer.formatDuration(duration)

                SetuManager.getActiveProvider()?.state?.value?.let { state ->
                    val notification = setuNotificationManager.buildNotification(state, "Call in Progress • $formattedTime")
                    // Use standard notification manager to update existing notification
                    val manager = getSystemService(NotificationManager::class.java)
                    manager.notify(SetuNotificationManager.NOTIFICATION_ID, notification)
                }
                delay(1000)
            }
        }
    }

    private fun shutdown() {
        timerJob?.cancel()
        timerJob = null
        serviceScope.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            // TODO
        }
        stopSelf()
    }

    override fun onDestroy() {
        shutdown()
        super.onDestroy()
    }

    companion object {
        const val ACTION_HANG_UP = "com.setu.voip.ACTION_HANG_UP"
        const val ACTION_ANSWER = "com.setu.voip.ACTION_ANSWER"
    }
}