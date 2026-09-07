package com.project.jh.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun JHCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = Color.White.copy(alpha = 0.05f),
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier.fillMaxWidth()
    val shape = RoundedCornerShape(24.dp)
    
    ElevatedCard(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun JHButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun JHTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.primary) } },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        ),
        singleLine = singleLine
    )
}

@Composable
fun SkillChip(
    skill: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Text(
            text = skill,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val backgroundColors = listOf(
        Color(0xFF8E44AD), // Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF2196F3), // Blue
        Color(0xFF00BFA5)  // Teal
    )
    val colorIndex = (initials.firstOrNull()?.code ?: 0) % backgroundColors.size
    val bgColor = backgroundColors[colorIndex]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor.copy(alpha = 0.2f))
            .border(BorderStroke(1.dp, bgColor.copy(alpha = 0.3f)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = bgColor,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2.2).sp
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
        "accepted", "active", "online" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFEF5350)
        "completed" -> Color(0xFF42A5F5)
        else -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
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
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            JHButton(text = actionText, onClick = onAction)
        }
    }
}
