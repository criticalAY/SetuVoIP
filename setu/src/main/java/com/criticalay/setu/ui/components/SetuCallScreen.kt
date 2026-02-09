package com.criticalay.setu.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.criticalay.setu.ui.SetuCallUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.criticalay.setu.R
import com.criticalay.setu.provider.CallStatus
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column

@Composable
fun SetuCallScreen(
    state: SetuCallUiState<*>,
    modifier: Modifier = Modifier,
    onMuteClick: () -> Unit = {},
    onSpeakerClick: () -> Unit = {},
    onAnswerClick: () -> Unit = {},
    onHangUpClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // User Information Section
            DefaultUserInfo(
                name = state.contactName,
                duration = state.durationText,
                status = state.statusText,
                callStatus = state.callStatus
            )

            // Controls Section
            DefaultControls(
                state = state,
                onMuteClick = onMuteClick,
                onSpeakerClick = onSpeakerClick,
                onAnswerClick = onAnswerClick,
                onHangUpClick = onHangUpClick
            )
        }
    }
}

@Composable
private fun DefaultUserInfo(
    name: String,
    duration: String,
    status: String,
    callStatus: CallStatus
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_avatar),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (callStatus == CallStatus.CONNECTED) duration else status,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun DefaultControls(
    state: SetuCallUiState<*>,
    onMuteClick: () -> Unit,
    onSpeakerClick: () -> Unit,
    onAnswerClick: () -> Unit,
    onHangUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.callStatus == CallStatus.RINGING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallActionButton(
                    iconRes = R.drawable.ic_call_end,
                    label = "Decline",
                    containerColor = Color(0xFFE53935),
                    onClick = onHangUpClick
                )
                CallActionButton(
                    iconRes = R.drawable.ic_call,
                    label = "Answer",
                    containerColor = Color(0xFF43A047),
                    onClick = onAnswerClick
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.showMuteButton) {
                    CallToggleButton(
                        activeIconRes = R.drawable.ic_mic_off,
                        inactiveIconRes = R.drawable.ic_mic,
                        isActive = state.isMuted,
                        onClick = onMuteClick
                    )
                }

                CallActionButton(
                    iconRes = R.drawable.ic_call_end,
                    label = "End",
                    containerColor = MaterialTheme.colorScheme.error,
                    onClick = onHangUpClick
                )

                if (state.showSpeakerButton) {
                    CallToggleButton(
                        activeIconRes = R.drawable.ic_speaker,
                        inactiveIconRes = R.drawable.ic_speaker,
                        isActive = state.isOnSpeaker,
                        onClick = onSpeakerClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    iconRes: Int,
    label: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun CallToggleButton(
    activeIconRes: Int,
    inactiveIconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
        )
    ) {
        Icon(
            painter = painterResource(id = if (isActive) activeIconRes else inactiveIconRes),
            contentDescription = null,
            tint = if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSimpleIncoming() {
    MaterialTheme {
        SetuCallScreen(state = SetuCallUiState<Unit>(
            contactName = "Alice",
            statusText = "Incoming...",
            callStatus = CallStatus.RINGING
        ))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSimpleActive() {
    MaterialTheme {
        SetuCallScreen(state = SetuCallUiState<Unit>(
            contactName = "Alice",
            durationText = "02:14",
            callStatus = CallStatus.CONNECTED
        ))
    }
}

private data class UserMetadata(val id: Int, val tag: String)

@Preview(showBackground = true, name = "State with Custom Object")
@Composable
fun PreviewStarProjectedCustom() {
    val state = SetuCallUiState(
        contactName = "Project Setu",
        durationText = "10:45",
        callStatus = CallStatus.CONNECTED,
        extraData = UserMetadata(101, "Priority")
    )

    MaterialTheme {
        SetuCallScreen(
            state = state,
            onHangUpClick = { /* User handles logic */ }
        )
    }
}

@Preview(showBackground = true, name = "Active - Only Hang Up Visible")
@Composable
fun PreviewOnlyHangUp() {
    MaterialTheme {
        SetuCallScreen(state = SetuCallUiState<Unit>(
            contactName = "Only Hangup",
            durationText = "04:20",
            callStatus = CallStatus.CONNECTED,
            showMuteButton = false,
            showSpeakerButton = false
        ))
    }
}

@Preview(showBackground = true, name = "Active - No Mute")
@Composable
fun PreviewNoMute() {
    MaterialTheme {
        SetuCallScreen(state = SetuCallUiState<Unit>(
            contactName = "No Mute Button",
            durationText = "01:15",
            callStatus = CallStatus.CONNECTED,
            showMuteButton = false,
            showSpeakerButton = true
        ))
    }
}