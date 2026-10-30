package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SubSection
import com.example.data.local.DailyShiftMetadataEntity
import com.example.ui.EntryStatusFilter
import com.example.ui.PetroLogViewModel
import com.example.ui.SlotProgressSummary
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500

@Composable
fun ShiftControlHeader(
    selectedDateIso: String,
    shiftMetadata: DailyShiftMetadataEntity,
    slotSummaries: List<SlotProgressSummary>,
    selectedSlotIndex: Int,
    currentSlotSummary: SlotProgressSummary,
    searchQuery: String,
    selectedSubSection: SubSection,
    selectedStatusFilter: EntryStatusFilter,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onSelectSlot: (Int) -> Unit,
    onEditSlotLabel: (Int) -> Unit,
    onUpdateOperators: (String, String, String, String, String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectSubSection: (SubSection) -> Unit,
    onSelectStatusFilter: (EntryStatusFilter) -> Unit,
    onCopyPreviousSlot: () -> Unit,
    modifier: Modifier = Modifier
) {
    var operatorSectionExpanded by rememberSaveable { mutableStateOf(false) }

    var dayName by remember(shiftMetadata.dateIso, shiftMetadata.dayOperatorName) {
        mutableStateOf(shiftMetadata.dayOperatorName)
    }
    var daySign by remember(shiftMetadata.dateIso, shiftMetadata.dayOperatorSignature) {
        mutableStateOf(shiftMetadata.dayOperatorSignature)
    }
    var nightName by remember(shiftMetadata.dateIso, shiftMetadata.nightOperatorName) {
        mutableStateOf(shiftMetadata.nightOperatorName)
    }
    var nightSign by remember(shiftMetadata.dateIso, shiftMetadata.nightOperatorSignature) {
        mutableStateOf(shiftMetadata.nightOperatorSignature)
    }
    var observations by remember(shiftMetadata.dateIso, shiftMetadata.shiftObservations) {
        mutableStateOf(shiftMetadata.shiftObservations)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 1. Date Selector + Operator Shift Summary Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Date Selector Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onPreviousDay,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("prev_date_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Jour précédent"
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenDatePicker() }
                                .testTag("date_picker_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "Sélectionner la date",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = PetroLogViewModel.formatDisplayDate(selectedDateIso),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Date : $selectedDateIso",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onNextDay,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("next_date_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Jour suivant"
                            )
                        }
                    }

                    // Expand/Collapse Operator & Signature Section
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (operatorSectionExpanded) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { operatorSectionExpanded = !operatorSectionExpanded }
                            .testTag("toggle_operators_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Badge,
                                contentDescription = "Opérateurs de quart",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.width(110.dp)) {
                                Text(
                                    text = "J: ${shiftMetadata.dayOperatorName.ifBlank { "À saisir" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "N: ${shiftMetadata.nightOperatorName.ifBlank { "À saisir" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = if (operatorSectionExpanded) {
                                    Icons.Filled.ExpandLess
                                } else {
                                    Icons.Filled.ExpandMore
                                },
                                contentDescription = "Déplier en-tête opérateurs",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Expandable Operator Name & Signature Panel (Quart de Jour / Quart de Nuit)
                AnimatedVisibility(visible = operatorSectionExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Text(
                            text = "EN-TÊTE DE POSTE : NOMS & SIGNATURES DES OPÉRATEURS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Quart de Jour Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = dayName,
                                onValueChange = {
                                    dayName = it
                                    onUpdateOperators(dayName, daySign, nightName, nightSign, observations)
                                },
                                label = { Text("Opérateur Quart de Jour") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.WbSunny,
                                        contentDescription = null,
                                        tint = Amber500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("day_operator_name_input")
                            )
                            OutlinedTextField(
                                value = daySign,
                                onValueChange = {
                                    daySign = it
                                    onUpdateOperators(dayName, daySign, nightName, nightSign, observations)
                                },
                                label = { Text("Signature / Visa Jour") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Draw,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("day_operator_signature_input")
                            )
                        }

                        // Quart de Nuit Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = nightName,
                                onValueChange = {
                                    nightName = it
                                    onUpdateOperators(dayName, daySign, nightName, nightSign, observations)
                                },
                                label = { Text("Opérateur Quart de Nuit") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.NightlightRound,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("night_operator_name_input")
                            )
                            OutlinedTextField(
                                value = nightSign,
                                onValueChange = {
                                    nightSign = it
                                    onUpdateOperators(dayName, daySign, nightName, nightSign, observations)
                                },
                                label = { Text("Signature / Visa Nuit") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Draw,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("night_operator_signature_input")
                            )
                        }

                        // Shift Observations
                        OutlinedTextField(
                            value = observations,
                            onValueChange = {
                                observations = it
                                onUpdateOperators(dayName, daySign, nightName, nightSign, observations)
                            },
                            label = { Text("Consignes / Observations de poste (optionnel)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shift_observations_input")
                        )
                    }
                }
            }
        }

        // 2. 4 Time Slot Tabs (Relevé 1 : 08H00, Relevé 2 : 14H00, Relevé 3 : 20H00, Relevé 4 : 02H00)
        if (slotSummaries.isNotEmpty()) {
            val safeTabIndex = (selectedSlotIndex - 1).coerceIn(0, slotSummaries.lastIndex)
            ScrollableTabRow(
                selectedTabIndex = safeTabIndex,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {},
                divider = {}
            ) {
                slotSummaries.forEach { slot ->
                val isSelected = slot.slotIndex == selectedSlotIndex
                val isSlotComplete = slot.completedCount == slot.totalCount && slot.totalCount > 0
                Tab(
                    selected = isSelected,
                    onClick = { onSelectSlot(slot.slotIndex) },
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                        .testTag("slot_tab_${slot.slotIndex}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Relevé ${slot.slotIndex} : ${slot.label}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isSlotComplete) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Complet",
                                        tint = Emerald500,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "${slot.completedCount}/${slot.totalCount} saisis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSlotComplete) {
                                        Emerald500
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        IconButton(
                            onClick = { onEditSlotLabel(slot.slotIndex) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("edit_slot_time_${slot.slotIndex}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Modifier l'heure du Relevé ${slot.slotIndex}",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            }
        }

        // 3. Progress Bar & Quick Actions for Active Slot
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Créneau ${currentSlotSummary.label} : ${currentSlotSummary.completedCount}/${currentSlotSummary.totalCount} saisis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("slot_progress_text")
                    )
                    if (currentSlotSummary.stoppedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${currentSlotSummary.stoppedCount} Arrêt/N/A",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (selectedSlotIndex > 1) {
                    TextButton(
                        onClick = onCopyPreviousSlot,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("copy_prev_slot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reporter R${selectedSlotIndex - 1}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else {
                    Text(
                        text = "${(currentSlotSummary.fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (currentSlotSummary.fraction >= 1f) Emerald500 else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { currentSlotSummary.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (currentSlotSummary.fraction >= 1f) Emerald500 else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }

        // 4. Search Bar & Sub-section + Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Rechercher par TAG (ex: PG1273, LG1017) ou équipement…",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Rechercher",
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Effacer la recherche",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_tag_input")
            )
        }

        // Sub-Section Filter Chips (Déméthaniseur, Dééthaniseur, Dépropaniseur, Débutaniseur, Déisopentaniseur, Réinjection LPG/Gasoline)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(SubSection.entries) { subSection ->
                val selected = subSection == selectedSubSection
                FilterChip(
                    selected = selected,
                    onClick = { onSelectSubSection(subSection) },
                    label = {
                        Text(
                            text = subSection.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("subsection_chip_${subSection.name}")
                )
            }
        }

        // Status Filter Row (Tous | À saisir | Saisis | En arrêt / N/A)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            items(EntryStatusFilter.entries) { statusFilter ->
                val selected = statusFilter == selectedStatusFilter
                AssistChip(
                    onClick = { onSelectStatusFilter(statusFilter) },
                    label = {
                        Text(
                            text = statusFilter.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        labelColor = if (selected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    modifier = Modifier.testTag("status_filter_${statusFilter.name}")
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    }
}
