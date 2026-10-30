package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.PlantParameter
import com.example.data.SubSection
import com.example.data.local.DailyShiftMetadataEntity
import com.example.data.local.ParameterReadingEntity
import com.example.ui.SlotProgressSummary
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.Parameter24HRow
import java.util.Locale

@Composable
fun Summary24HScreen(
    metadata: DailyShiftMetadataEntity,
    rows24H: List<Parameter24HRow>,
    slotSummaries: List<SlotProgressSummary>,
    selectedSubSection: SubSection,
    onSelectSubSection: (SubSection) -> Unit,
    onCellClick: (PlantParameter, Int, ParameterReadingEntity?) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onOpenPreviewDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Shift Summary & Export Action Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FEUILLE RÉCAPITULATIVE 24H — ${metadata.dateIso}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Jour: ${metadata.dayOperatorName.ifBlank { "Non renseigné" }} (${metadata.dayOperatorSignature.ifBlank { "Non signé" }})  •  " +
                                "Nuit: ${metadata.nightOperatorName.ifBlank { "Non renseigné" }} (${metadata.nightOperatorSignature.ifBlank { "Non signé" }})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 4-Slot Completion Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slotSummaries.forEach { slot ->
                        val complete = slot.completedCount == slot.totalCount && slot.totalCount > 0
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (complete) {
                                Emerald500.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = slot.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (complete) Emerald500 else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${slot.completedCount}/${slot.totalCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Export Buttons: CSV (Excel), PDF 24H, Aperçu/Partage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TableChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CSV (Excel)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PDF 24H",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenPreviewDialog,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(0.85f)
                            .testTag("preview_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Aperçu",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Sub-section Filter Row for the 24H Table
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(SubSection.entries.size) { idx ->
                val sub = SubSection.entries[idx]
                FilterChip(
                    selected = sub == selectedSubSection,
                    onClick = { onSelectSubSection(sub) },
                    label = {
                        Text(
                            text = sub.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }

        // 2. Cross-Tabulated 24H Industrial Table (Équipement | TAG | 08H00 | 14H00 | 20H00 | 02H00 | Moy/Δ)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(modifier = Modifier.width(740.dp)) {
                // Sticky Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("N°", Modifier.width(36.dp))
                    TableHeaderCell("ÉQUIPEMENT", Modifier.width(210.dp))
                    TableHeaderCell("TAG", Modifier.width(104.dp))
                    TableHeaderCell(metadata.getSlotLabel(1), Modifier.width(78.dp), TextAlign.Center)
                    TableHeaderCell(metadata.getSlotLabel(2), Modifier.width(78.dp), TextAlign.Center)
                    TableHeaderCell(metadata.getSlotLabel(3), Modifier.width(78.dp), TextAlign.Center)
                    TableHeaderCell(metadata.getSlotLabel(4), Modifier.width(78.dp), TextAlign.Center)
                    TableHeaderCell("Δ 24H", Modifier.width(62.dp), TextAlign.End)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("summary_24h_table_list"),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(
                        items = rows24H,
                        key = { _, row -> row.parameter.id }
                    ) { index, row ->
                        val p = row.parameter
                        val bgColor = if (index % 2 == 0) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // N°
                            Text(
                                text = String.format(Locale.US, "%02d", p.number),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp)
                            )

                            // Équipement + Unité
                            Column(
                                modifier = Modifier
                                    .width(210.dp)
                                    .padding(end = 6.dp)
                            ) {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${p.subSection.label} • ${p.measureType.unit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // TAG in bold monospace
                            Text(
                                text = p.tag,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(104.dp)
                            )

                            // 4 Slot Cells (Tappable to edit directly)
                            TableSlotValueCell(
                                reading = row.slot1,
                                onClick = { onCellClick(p, 1, row.slot1) },
                                modifier = Modifier.width(78.dp)
                            )
                            TableSlotValueCell(
                                reading = row.slot2,
                                onClick = { onCellClick(p, 2, row.slot2) },
                                modifier = Modifier.width(78.dp)
                            )
                            TableSlotValueCell(
                                reading = row.slot3,
                                onClick = { onCellClick(p, 3, row.slot3) },
                                modifier = Modifier.width(78.dp)
                            )
                            TableSlotValueCell(
                                reading = row.slot4,
                                onClick = { onCellClick(p, 4, row.slot4) },
                                modifier = Modifier.width(78.dp)
                            )

                            // 24H Delta
                            Text(
                                text = row.deltaFormatted,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(62.dp)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
private fun TableSlotValueCell(
    reading: ParameterReadingEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStopped = reading?.isStoppedOrNa == true
    val isFilled = reading?.isCompleted == true
    val text = reading?.displayTableValue ?: "—"

    Box(
        modifier = modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    isStopped -> Amber500.copy(alpha = 0.2f)
                    isFilled -> Emerald500.copy(alpha = 0.14f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    isStopped -> Amber500.copy(alpha = 0.5f)
                    isFilled -> Emerald500.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.labelMedium,
            color = when {
                isStopped -> Amber500
                isFilled -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
