package com.wordgame.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordgame.ui.theme.TimerAlertRed

@Composable
fun TimerView(
    timeRemaining: Int,
    totalTime: Int,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = timeRemaining.toFloat() / totalTime,
        label = "timer_progress"
    )
    
    val timerColor by animateColorAsState(
        targetValue = when {
            timeRemaining <= 10 -> TimerAlertRed
            timeRemaining <= 30 -> Color(0xFFFFA000)
            else -> Color(0xFF4CAF50)
        },
        label = "timer_color"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Time",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "$timeRemaining",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = timerColor
        )
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(top = 4.dp),
            color = timerColor
        )
    }
}
