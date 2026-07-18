package dev.mizzenmast.calculator

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Feature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val features = listOf(
    Feature(
        Icons.Default.Calculate,
        "Calculator",
        "A clean calculator with memory functions and full expression support"
    ),
    Feature(
        Icons.Default.Straighten,
        "Unit Converter",
        "Convert length, weight, temperature and volume instantly"
    ),
    Feature(
        Icons.Default.CurrencyExchange,
        "Currency Exchange",
        "Live exchange rates, favorites and useful trend indicators"
    )
)

fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager

    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_INTERNET
    )
}

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    var online by remember {
        mutableStateOf(isOnline(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(28.dp))

        /*
         * Hero icon
         */
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "SmartCalc",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Everything you need for everyday calculations.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(30.dp))

        /*
         * Feature cards
         */
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            features.forEach { feature ->
                FeatureCard(feature)
            }
        }

        Spacer(Modifier.weight(1f))

        /*
         * Connection status
         */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (online) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (online) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (online) {
                            Icons.Default.Wifi
                        } else {
                            Icons.Default.WifiOff
                        },
                        contentDescription = null,
                        tint = if (online) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (online) {
                            "You're connected"
                        } else {
                            "You're offline"
                        },
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (online) {
                            "Live exchange rates are available"
                        } else {
                            "Currency rates will be unavailable"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (online) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    TextButton(
                        onClick = {
                            online = isOnline(context)
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        /*
         * Main CTA
         */
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FeatureCard(
    feature: Feature
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = feature.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}