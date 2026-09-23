package com.duetify.app.games.ui.thisorthat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duetify.app.games.engine.ThisOrThatState
import com.duetify.app.ui.theme.AppBackground
import com.duetify.app.ui.theme.Coral
import com.duetify.app.ui.theme.OnAccent
import com.duetify.app.ui.theme.OnSurfaceLight
import com.duetify.app.ui.theme.OnSurfaceVariantPink
import com.duetify.app.ui.theme.SurfaceContainer
import com.duetify.app.ui.theme.SurfaceHigh
import com.duetify.app.ui.theme.Teal

@Composable
fun ThisOrThatScreen(
    onBack: () -> Unit,
    viewModel: ThisOrThatViewModel = hiltViewModel(),
) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                title = "This or That",
                onBack = {
                    if (phase is DuetPhase.Lobby) onBack() else viewModel.leave()
                },
            )
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                when (val p = phase) {
                    is DuetPhase.Lobby -> LobbyView(
                        onlineAvailable = viewModel.onlineAvailable,
                        onBot = viewModel::playWithBot,
                        onCreate = viewModel::createRoom,
                        onJoin = viewModel::joinRoom,
                    )
                    is DuetPhase.Connecting -> CenterStatus(p.message, spinner = true)
                    is DuetPhase.WaitingForPartner ->
                        WaitingView(code = p.code, online = p.online, onCancel = viewModel::leave)
                    is DuetPhase.Playing -> PlayingView(
                        game = p.game,
                        partnerName = p.partnerName,
                        onAnswer = viewModel::answer,
                    )
                    is DuetPhase.Finished -> FinishedView(
                        game = p.game,
                        partnerName = p.partnerName,
                        onPlayAgain = viewModel::playAgain,
                        onDone = onBack,
                    )
                    is DuetPhase.Failed -> CenterStatus(p.message, spinner = false, onRetry = viewModel::leave)
                }
            }
        }
    }
}

@Composable
private fun Header(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = OnSurfaceLight,
            )
        }
        Spacer(Modifier.size(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceLight,
        )
    }
}

@Composable
private fun LobbyView(
    onlineAvailable: Boolean,
    onBot: () -> Unit,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit,
) {
    var code by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Answer a dozen quick “this or that” questions at the same time, then see how in-sync you two are.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariantPink,
        )
        Spacer(Modifier.height(24.dp))

        PrimaryAction(
            icon = { Icon(Icons.Filled.SmartToy, null, tint = OnAccent) },
            label = "Play with a bot",
            subtitle = "Try it right now, solo",
            onClick = onBot,
        )
        Spacer(Modifier.height(12.dp))
        PrimaryAction(
            icon = { Icon(Icons.Filled.Favorite, null, tint = OnAccent) },
            label = "Create a room",
            subtitle = if (onlineAvailable) {
                "Get a code — play live with your partner anywhere"
            } else {
                "Get a code to share (same device for now)"
            },
            onClick = onCreate,
        )
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Have a code?",
            style = MaterialTheme.typography.labelLarge,
            color = OnSurfaceLight,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(4) },
                singleLine = true,
                placeholder = { Text("ABCD") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(10.dp))
            Button(
                onClick = { if (code.isNotBlank()) onJoin(code) },
                enabled = code.length == 4,
                colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
            ) { Text("Join") }
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = if (onlineAvailable) {
                "Live rooms are on: create a room, send the code, and play together from anywhere."
            } else {
                "Long-distance play needs Firebase — add google-services.json to app/ to switch rooms to live cross-device sync. Until then, rooms connect on this device and the bot is always ready."
            },
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantPink,
        )
    }
}

@Composable
private fun PrimaryAction(
    icon: @Composable () -> Unit,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Coral),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.size(14.dp))
        Column {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantPink)
        }
    }
}

@Composable
private fun WaitingView(code: String, online: Boolean, onCancel: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Share this code", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
        Spacer(Modifier.height(16.dp))
        Text(
            text = code,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = Coral,
            letterSpacing = 8.sp,
        )
        Spacer(Modifier.height(16.dp))
        CircularProgressIndicator(color = Teal)
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (online) {
                "Waiting for your partner to join from their phone…"
            } else {
                "Waiting for a partner to join on this device…"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantPink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = onCancel) { Text("Cancel") }
    }
}

@Composable
private fun PlayingView(
    game: ThisOrThatState,
    partnerName: String,
    onAnswer: (Boolean) -> Unit,
) {
    val prompt = game.currentPrompt
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(8.dp))
        val total = game.totalRounds.coerceAtLeast(1)
        LinearProgressIndicator(
            progress = { game.roundIndex.toFloat() / total.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            color = Coral,
            trackColor = SurfaceContainer,
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Round ${(game.roundIndex + 1).coerceAtMost(total)} / $total",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceVariantPink,
            )
            if (game.revealed.isNotEmpty()) {
                Text(
                    "${game.compatibilityPercent}% in sync",
                    style = MaterialTheme.typography.labelLarge,
                    color = Teal,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        if (prompt == null) return

        if (game.selfAnsweredCurrent && !game.partnerAnsweredCurrent) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = Teal)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Locked in — waiting for $partnerName…",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceVariantPink,
                    textAlign = TextAlign.Center,
                )
            }
            return
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            ChoiceCard(text = prompt.left, onClick = { onAnswer(true) })
            Spacer(Modifier.height(16.dp))
            Text(
                "or",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariantPink,
            )
            Spacer(Modifier.height(16.dp))
            ChoiceCard(text = prompt.right, onClick = { onAnswer(false) })
        }
    }
}

@Composable
private fun ChoiceCard(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceLight,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FinishedView(
    game: ThisOrThatState,
    partnerName: String,
    onPlayAgain: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text("You & $partnerName", style = MaterialTheme.typography.titleMedium, color = OnSurfaceVariantPink)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${game.compatibilityPercent}%",
            fontSize = 88.sp,
            fontWeight = FontWeight.Bold,
            color = Coral,
        )
        Text(
            text = matchBlurb(game.compatibilityPercent),
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceLight,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${game.matches} of ${game.revealed.size} answers matched",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantPink,
        )
        Spacer(Modifier.height(24.dp))

        game.revealed.forEach { result ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainer)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (result.matched) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (result.matched) Teal else OnSurfaceVariantPink,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (result.selfPickedLeft) result.prompt.left else result.prompt.right,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight,
                    )
                    if (!result.matched) {
                        Text(
                            text = "$partnerName: " +
                                (if (result.partnerPickedLeft) result.prompt.left else result.prompt.right),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariantPink,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = OnAccent),
        ) { Text("Play again") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Back to games") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CenterStatus(message: String, spinner: Boolean, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (spinner) {
            CircularProgressIndicator(color = Coral)
            Spacer(Modifier.height(16.dp))
        }
        Text(message, style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) { Text("Back") }
        }
    }
}

private fun matchBlurb(percent: Int): String = when {
    percent >= 85 -> "Two peas in a pod"
    percent >= 65 -> "Seriously in sync"
    percent >= 45 -> "A lovely balance"
    percent >= 25 -> "Opposites attract"
    else -> "You keep things interesting"
}
