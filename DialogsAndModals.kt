package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.PlantParameter
import com.example.data.local.ParameterReadingEntity
import com.example.ui.PetroLogViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShiftDatePickerModal(
    currentDateIso: String,
    loggedDates: List<String>,
    onSelectDate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(currentDateIso) {
        runCatching {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(currentDateIso)?.time
        }.getOrNull() ?: System.currentTimeMillis()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        onSelectDate(sdf.format(Date(millis)))
                    }
                    onDismiss()
                }
            ) {
                Text("Valider la date")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = {
                        onSelectDate(PetroLogViewModel.todayIso())
                        onDismiss()
                    }
                ) {
                    Text("Aujourd'hui")
                }
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
            }
        }
    ) {
        Column {
            DatePicker(state = datePickerState)
            if (loggedDates.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        text = "Journées avec relevés enregistrés :",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        loggedDates.take(5).forEach { dateStr ->
                            FilterChip(
                                selected = dateStr == currentDateIso,
                                onClick = {
                                    onSelectDate(dateStr)
                                    onDismiss()
                                },
                                label = { Text(dateStr, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditSlotTimeLabelDialog(
    slotIndex: Int,
    currentLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(slotIndex, currentLabel) { mutableStateOf(currentLabel) }
    val presets = listOf("08H00", "14H00", "20H00", "02H00", "06H00", "12H00", "18H00", "00H00")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modifier l'heure du Relevé $slotIndex",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Personnalisez l'intitulé horaire affiché sur les onglets, le tableau 24H et les exports CSV/PDF :",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.uppercase(Locale.ROOT) },
                    label = { Text("Intitulé de l'heure (ex: 08H00)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_label_dialog_input")
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = text == preset,
                            onClick = { text = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(text)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_slot_label_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun QuickEditCellModal(
    parameter: PlantParameter,
    slotIndex: Int,
    slotLabel: String,
    currentReading: ParameterReadingEntity?,
    onSave: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var valueText by remember(parameter.id, slotIndex, currentReading) {
        mutableStateOf(currentReading?.rawValue.orEmpty())
    }
    var isStopped by remember(parameter.id, slotIndex, currentReading) {
        mutableStateOf(currentReading?.isStoppedOrNa == true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${parameter.tag} • Créneau $slotLabel",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "#${parameter.number} — ${parameter.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = if (isStopped) "ARRÊT / N/A" else valueText,
                    onValueChange = { input ->
                        valueText = input.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }
                    },
                    enabled = !isStopped,
                    label = { Text("Valeur (${parameter.measureType.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isStopped,
                        onCheckedChange = { isStopped = it }
                    )
                    Text(
                        text = "Équipement en arrêt / N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(valueText, isStopped)
                    onDismiss()
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ReportPreviewAndShareModal(
    dateIso: String,
    textSummary: String,
    csvPreview: String,
    onCopySummary: () -> Unit,
    onShareText: () -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onDismiss: () -> Unit
) {
    var showCsvTab by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Export & Partage 24H ($dateIso)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    FilterChip(
                        selected = !showCsvTab,
                        onClick = { showCsvTab = false },
                        label = { Text("Résumé Texte 24H") }
                    )
                    FilterChip(
                        selected = showCsvTab,
                        onClick = { showCsvTab = true },
                        label = { Text("Aperçu CSV (Excel)") }
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 300.dp)
                ) {
                    Text(
                        text = if (showCsvTab) csvPreview else textSummary,
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopySummary,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copier")
                    }
                    OutlinedButton(
                        onClick = onShareText,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Message")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportCsv,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CSV Excel")
                    }
                    Button(
                        onClick = onExportPdf,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF 24H")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
