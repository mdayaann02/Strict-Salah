package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

data class HadithItem(
    val quote: String,
    val source: String,
    val lesson: String
)

private val HADITH_COLLECTION = listOf(
    HadithItem(
        quote = "The first matter that the slave will be brought to account for on the Day of Judgment is the prayer. If it is sound, then the rest of his deeds will be sound.",
        source = "Sunan an-Nasa'i 465 • Sahih",
        lesson = "Guard every single Salah; it is the cornerstone of all your deeds."
    ),
    HadithItem(
        quote = "The most beloved of deeds to Allah is the prayer offered at its proper appointed time.",
        source = "Sahih al-Bukhari 527 • Agreed Upon",
        lesson = "Do not delay your prayer for worldly distractions."
    ),
    HadithItem(
        quote = "Between a person and disbelief or polytheism is the abandonment of prayer.",
        source = "Sahih Muslim 82",
        lesson = "Prayer is the distinct covenant of faith for every Muslim."
    ),
    HadithItem(
        quote = "Whoever prays the two cool prayers (Fajr and Asr) will enter Paradise.",
        source = "Sahih al-Bukhari 574",
        lesson = "Fajr at dawn and Asr in the afternoon carry immense spiritual protection."
    ),
    HadithItem(
        quote = "The five daily prayers are like an abundant river flowing past the door of any one of you in which he bathes five times a day. Would any dirt remain on him?",
        source = "Sahih Muslim 667",
        lesson = "Each prayer washes away daily shortcomings and purifies the heart."
    ),
    HadithItem(
        quote = "When any one of you stands to pray, he is in intimate conversation with his Lord.",
        source = "Sahih al-Bukhari 405",
        lesson = "Stand in Salah with presence of mind, humility, and complete devotion."
    ),
    HadithItem(
        quote = "Prayer in congregation is twenty-seven degrees superior to prayer said alone.",
        source = "Sahih al-Bukhari 645",
        lesson = "Whenever possible, join the congregation at your local masjid."
    )
)

@Composable
fun HadithCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    var hadithIndex by remember { mutableIntStateOf(dayOfYear % HADITH_COLLECTION.size) }

    val currentHadith = HADITH_COLLECTION[hadithIndex % HADITH_COLLECTION.size]

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hadith_of_the_day_card"),
        shape = RoundedCornerShape(22.dp),
        glowColor = Color(0xFF10B981)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Column {
                // Header with badge & action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B5E20).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hadith of the Day",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Daily Inspiration & Reflection",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                hadithIndex = (hadithIndex + 1) % HADITH_COLLECTION.size
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Next Hadith",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendText = "\"${currentHadith.quote}\"\n\n— ${currentHadith.source}\n\nShared from Strict Salah App"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, sendText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Hadith"))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quote Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1B5E20).copy(alpha = 0.06f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B5E20).copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = Color(0xFF1B5E20).copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentHadith.quote,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "— ${currentHadith.source}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Key takeaway / Reflection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "💡 Reflection: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentHadith.lesson,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
