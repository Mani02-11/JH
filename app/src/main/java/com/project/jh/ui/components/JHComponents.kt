package com.project.jh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.jh.ui.theme.JHPrimary
import com.project.jh.ui.theme.JHSecondary

@Composable
fun JHCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    } else {
        ElevatedCard(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
fun JHButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = JHPrimary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SkillChip(
    skill: String,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = { },
        label = { Text(skill, fontSize = 12.sp) },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = JHSecondary.copy(alpha = 0.1f),
            labelColor = JHSecondary
        ),
        border = null
    )
}

@Composable
fun ProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(JHPrimary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = JHPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2.5).sp
        )
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val color = when (status.lowercase()) {
        "pending" -> Color(0xFFFBC02D)
        "accepted", "active", "online" -> JHSecondary
        "rejected" -> Color.Red
        "completed" -> Color.Blue
        else -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = status,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            JHButton(text = actionText, onClick = onAction)
        }
    }
}
