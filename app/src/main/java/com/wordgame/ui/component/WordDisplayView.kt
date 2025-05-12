package com.wordgame.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordgame.ui.theme.LetterBoxBackground
import com.wordgame.ui.theme.LetterBoxRevealedBackground

@Composable
fun WordDisplayView(
    word: String,
    revealedIndices: Set<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val letterSize = when {
            word.length <= 6 -> 48.dp
            word.length <= 8 -> 40.dp
            else -> 32.dp
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        word.forEachIndexed { index, char ->
            val isRevealed = index in revealedIndices
            LetterBox(
                letter = char,
                isRevealed = isRevealed,
                modifier = Modifier.size(letterSize)
            )
            
            if (index < word.length - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun LetterBox(
    letter: Char,
    isRevealed: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isRevealed) LetterBoxRevealedBackground else LetterBoxBackground
    
    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            ),
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = if (isRevealed) letter.toString().uppercase() else "",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
