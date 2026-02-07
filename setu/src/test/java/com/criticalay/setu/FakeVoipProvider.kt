package com.criticalay.setu

import com.criticalay.setu.provider.CallState
import com.criticalay.setu.provider.VoipProvider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeVoipProvider : VoipProvider {
    private val _state = MutableStateFlow(CallState())
    override val state: StateFlow<CallState> = _state.asStateFlow()
    override fun startCall(handle: String) {
    }

    var wasEndCallCalled = false
    var wasAnswerCallCalled = false
    var toggledMuteTo: Boolean? = null
    var toggledSpeakerTo: Boolean? = null

    fun updateState(newState: CallState) {
        _state.value = newState
    }

    override fun answerCall() {
        wasAnswerCallCalled = true
    }

    override fun rejectCall() {
    }

    override fun endCall() {
        wasEndCallCalled = true
    }

    override fun toggleMute(muted: Boolean) {
        toggledMuteTo = muted
        _state.value = _state.value.copy(isMuted = muted)
    }

    override fun toggleSpeaker(enabled: Boolean) {
        toggledSpeakerTo = enabled
        _state.value = _state.value.copy(isOnSpeaker = enabled)
    }

    override fun release() {
    }
}