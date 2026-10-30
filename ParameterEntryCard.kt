package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ParameterMeasureType
import com.example.data.PlantParameter
import com.example.data.local.ParameterReadingEntity
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Sky600
import java.util.Locale

@Composable
fun ParameterEntryCard(
    parameter: PlantParameter,
    reading: ParameterReadingEntity?,
    previousSlotReading: ParameterReadingEntity?,
    previousSlotLabel: String?,
    focusRequester: FocusRequester,
    isLastItem: Boolean,
    onValueChanged: (String, Boolean) -> Unit,
    onMoveToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStopped = reading?.isStoppedOrNa == true
    val storedValue = reading?.rawValue.orEmpty()
    val isCompleted = reading?.isCompleted == true

    // Keep local text state synchronized with database while allowing smooth decimal typing
    var textValue by remember(parameter.id, reading?.slotIndex, reading?.dateIso, isStopped) {
        mutableStateOf(storedValue)
    }
    if (textValue != storedValue && (storedValue.isEmpty() || !textValue.endsWith("."))) {
        textValue = storedValue
    }

    val measureIcon = when (parameter.measureType) {
        ParameterMeasureType.LEVEL -> Icons.Filled.WaterDrop
        ParameterMeasureType.TEMPERATURE -> Icons.Filled.Thermostat
        ParameterMeasureType.PRESSURE -> Icons.Filled.Speed
    }

    val borderColor = when {
        isStopped -> Amber600.copy(alpha = 0.7f)
        isCompleted -> Emerald600.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("param_card_${parameter.tag}"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isStopped -> Amber50
                isCompleted -> Emerald50
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Top Row: Index Badge + Equipment Name + Sub-section badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Number badge (#01 .. #47)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCompleted) {
                            if (isStopped) Amber500 else Emerald500
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = String.format(Locale.US, "%02d", parameter.number),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = parameter.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Sub-section short badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = parameter.subSection.shortCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Bold Monospace TAG + Previous Slot Info + Decimal Input + "En arrêt / N/A" Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TAG Badge + Previous Reading Reference
                Column(
                    modifier = Modifier.weight(0.95f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = measureIcon,
                                contentDescription = parameter.measureType.label,
                                tint = Sky600,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "TAG : ${parameter.tag}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (previousSlotReading != null && previousSlotReading.isCompleted && previousSlotLabel != null) {
                        Text(
                            text = "Préc ($previousSlotLabel): ${previousSlotReading.displayTableValue} ${if (previousSlotReading.isStoppedOrNa) "" else parameter.measureType.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "${parameter.measureType.label} (${parameter.measureType.unit})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Numeric Decimal Field (or "EN ARRÊT / N/A" state)
                OutlinedTextField(
                    value = if (isStopped) "ARRÊT / N/A" else textValue,
                    onValueChange = { newText ->
                        // Allow digits, dot, comma, minus
                        val filtered = newText.filter { ch ->
                            ch.isDigit() || ch == '.' || ch == ',' || ch == '-'
                        }
                        textValue = filtered
                        onValueChanged(filtered, false)
                    },
                    enabled = !isStopped,
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "0.0",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    suffix = {
                        if (!isStopped) {
                            Text(
                                text = parameter.measureType.unit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = if (isLastItem) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onMoveToNext() },
                        onDone = { onMoveToNext() }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        disabledTextColor = Amber500,
                        disabledBorderColor = Amber500.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1.05f)
                        .focusRequester(focusRequester)
                        .testTag("input_value_${parameter.tag}")
                )

                // "En arrêt / N/A" Ergonomic Checkbox
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isStopped) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val newStopped = !isStopped
                            onValueChanged(textValue, newStopped)
                        }
                        .testTag("checkbox_na_${parameter.tag}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Checkbox(
                            checked = isStopped,
                            onCheckedChange = { checked ->
                                onValueChanged(textValue, checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Amber500,
                                checkmarkColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Arrêt/N/A",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isStopped) FontWeight.Bold else FontWeight.Normal,
                            color = if (isStopped) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PartSectionDividerCard(
    partNumber: Int,
    subtitle: String,
    parameterCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PARTIE $partNumber — $subtitle",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "$parameterCount TAGs",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}
