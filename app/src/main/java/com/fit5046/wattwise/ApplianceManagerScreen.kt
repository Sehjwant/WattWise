package com.fit5046.wattwise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
data class CategoryStyle(
    val color: Color,
    val backgroundColor: Color,
    val icon: ImageVector
)

fun getCategoryStyle(category: String): CategoryStyle = when (category) {
    "Cooling"  -> CategoryStyle(Color(0xFF1565C0), Color(0xFFE3F2FD), Icons.Default.AcUnit)
    "Heating"  -> CategoryStyle(Color(0xFFB71C1C), Color(0xFFFFEBEE), Icons.Default.Fireplace)
    "Laundry"  -> CategoryStyle(Color(0xFF6A1B9A), Color(0xFFF3E5F5), Icons.Default.LocalLaundryService)
    "Kitchen"  -> CategoryStyle(Color(0xFFE65100), Color(0xFFFFF3E0), Icons.Default.Kitchen)
    "Lighting" -> CategoryStyle(Color(0xFFF9A825), Color(0xFFFFFDE7), Icons.Default.LightMode)
    else       -> CategoryStyle(Color(0xFF455A64), Color(0xFFECEFF1), Icons.Default.Power)
}

