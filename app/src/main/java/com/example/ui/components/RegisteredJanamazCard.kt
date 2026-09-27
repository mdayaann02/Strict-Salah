package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity

@Composable
fun RegisteredJanamazCard(
    profile: UserProfileEntity,
    registeredBitmaps: List<Bitmap>,
    onOpenRegistration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("registered_janamaz_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.isJanamazRegistered)
                Color(0xFF1B5E20).copy(alpha = 0.08f)
            else
                Color(0xFFE65100).copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (profile.isJanamazRegistered) Color(0xFF1B5E20).copy(alpha = 0.25f) else Color(0xFFE65100).copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (profile.isJanamazRegistered) Color(0xFF1B5E20).copy(alpha = 0.2f)
                                else Color(0xFFE65100).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (profile.isJanamazRegistered) Icons.Default.Verified else Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (profile.isJanamazRegistered) Color(0xFF1B5E20) else Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (profile.isJanamazRegistered) "Registered Janamaz Mat" else "Register Your Janamaz",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (profile.isJanamazRegistered) Color(0xFF1B5E20).copy(alpha = 0.2f) else Color(0xFFE65100).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (profile.isJanamazRegistered) "Active" else "Required",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (profile.isJanamazRegistered) Color(0xFF1B5E20) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (profile.isJanamazRegistered)
                                "${registeredBitmaps.size} Mat(s) configured for AI matching"
                            else
                                "Upload photo to enable exact AI comparison",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                if (profile.isJanamazRegistered) {
                    OutlinedButton(
                        onClick = onOpenRegistration,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("edit_janamaz_button")
                    ) {
                        Text("Update", fontSize = 11.sp)
                    }
                }
            }

            if (profile.isJanamazRegistered && registeredBitmaps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(registeredBitmaps) { bmp ->
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 76.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF1B5E20).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Configured Janamaz",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            if (!profile.isJanamazRegistered) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Upload 1-3 photos of your prayer mat now so the app accurately recognizes it during prayer lockdowns instead of generic detection.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenRegistration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("initial_register_janamaz_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Register Janamaz Photo Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
