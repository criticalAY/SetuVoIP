package com.criticalay.setu.core

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.criticalay.setu.provider.VoipProvider
import com.criticalay.setu.service.VoipService
import com.criticalay.setu.util.CallNotificationConfig
import java.lang.ref.WeakReference

object SetuManager {
    private var appContext: WeakReference<Context>? = null
    private var activeProvider: VoipProvider? = null
    private var isDebugMode = false

    var notificationConfig: CallNotificationConfig = CallNotificationConfig()
        private set

    fun init(context: Context) {
        appContext = WeakReference(context.applicationContext)
    }

    /**
     * Set or clear the active provider.
     * Used internally by the library and for unit testing.
     */
    fun setActiveProvider(provider: VoipProvider?) {
        this.activeProvider = provider
    }

    /**
     * Start a call with an optional custom configuration.
     */
    fun startCall(provider: VoipProvider, config: CallNotificationConfig = CallNotificationConfig()) {
        val context = appContext?.get() ?: throw IllegalStateException("Call SetuManager.init first")

        this.activeProvider = provider
        this.notificationConfig = config

        val intent = Intent(context, VoipService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun getActiveProvider(): VoipProvider? = activeProvider

    fun endCall() {
        activeProvider?.endCall()
        activeProvider = null
    }

    @VisibleForTesting
    fun reset() {
        activeProvider = null
        appContext = null
        notificationConfig = CallNotificationConfig()
    }
}