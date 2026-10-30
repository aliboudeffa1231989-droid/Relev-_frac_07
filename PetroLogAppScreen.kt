package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PlantParameter
import com.example.data.SubSection
import com.example.data.local.ParameterReadingEntity
import com.example.ui.EntryStatusFilter
import com.example.ui.MainDestination
import com.example.ui.PetroLogViewModel
import com.example.ui.components.EditSlotTimeLabelDialog
import com.example.ui.components.ParameterEntryCard
import com.example.ui.components.PartSectionDividerCard
import com.example.ui.components.QuickEditCellModal
import com.example.ui.components.ReportPreviewAndShareModal
import com.example.ui.components.ShiftControlHeader
import com.example.ui.components.ShiftDatePickerModal
import com.example.ui.theme.Emerald500
import com.example.util.ReportExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetroLogAppScreen(
    viewModel: PetroLogViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var showDatePicker by remember { mutableStateOf(false) }
    var editingSlotIndex by remember { mutableStateOf<Int?>(null) }
    var editingCellTarget by remember { mutableStateOf<Triple<PlantParameter, Int, ParameterReadingEntity?>?>(null) }
    var showExportModal by remember { mutableStateOf(false) }

    // BackHandler for secondary screen (24H Summary Table -> Field Entry)
    if (uiState.activeDestination == MainDestination.SUMMARY_TABLE_24H) {
        BackHandler {
            viewModel.selectDestination(MainDestination.FIELD_ENTRY)
        }
    }

    val focusRequesters = remember(uiState.allParameters) {
        uiState.allParameters.associate { it.id to FocusRequester() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PrecisionManufacturing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "COLONNES, BALLONS, REBOUILLEURS & POMPES",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Emerald500)
                                )
                                Text(
                                    text = "Sauvegarde locale auto (Hors-ligne)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportModal = true },
                        modifier = Modifier.testTag("top_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.IosShare,
                            contentDescription = "Exporter CSV ou PDF 24H"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDarkTheme) "Passer en mode clair" else "Passer en mode sombre"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = uiState.activeDestination == MainDestination.FIELD_ENTRY,
                    onClick = { viewModel.selectDestination(MainDestination.FIELD_ENTRY) },
                    icon = {
                        BadgedBox(
                            badge = {
                                val summary = uiState.currentSlotSummary
                                Badge(
                                    containerColor = if (summary.completedCount == summary.totalCount) {
                                        Emerald500
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                ) {
                                    Text("${summary.completedCount}/${summary.totalCount}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = "Saisie Terrain"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Saisie Terrain (${uiState.currentSlotSummary.label})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.testTag("nav_field_entry")
                )

                NavigationBarItem(
                    selected = uiState.activeDestination == MainDestination.SUMMARY_TABLE_24H,
                    onClick = { viewModel.selectDestination(MainDestination.SUMMARY_TABLE_24H) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.TableView,
                            contentDescription = "Tableau récapitulatif 24H"
                        )
                    },
                    label = {
                        Text(
                            text = "Tableau Récapitulatif 24H",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.testTag("nav_summary_24h")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState.activeDestination) {
                MainDestination.FIELD_ENTRY -> {
                    ShiftControlHeader(
                        selectedDateIso = uiState.selectedDateIso,
                        shiftMetadata = uiState.shiftMetadata,
                        slotSummaries = uiState.slotSummaries,
                        selectedSlotIndex = uiState.selectedSlotIndex,
                        currentSlotSummary = uiState.currentSlotSummary,
                        searchQuery = uiState.searchQuery,
                        selectedSubSection = uiState.selectedSubSection,
                        selectedStatusFilter = uiState.selectedStatusFilter,
                        onPreviousDay = { viewModel.stepDateByDays(-1) },
                        onNextDay = { viewModel.stepDateByDays(1) },
                        onOpenDatePicker = { showDatePicker = true },
                        onSelectSlot = { viewModel.selectSlot(it) },
                        onEditSlotLabel = { editingSlotIndex = it },
                        onUpdateOperators = { dName, dSign, nName, nSign, obs ->
                            viewModel.updateShiftOperators(dName, dSign, nName, nSign, obs)
                        },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onSelectSubSection = { viewModel.selectSubSection(it) },
                        onSelectStatusFilter = { viewModel.selectStatusFilter(it) },
                        onCopyPreviousSlot = {
                            viewModel.copyFromPreviousSlot()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Valeurs du créneau précédent reportées sur les TAGs vides.")
                            }
                        }
                    )

                    if (uiState.filteredParameters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterAltOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(42.dp)
                                )
                                Text(
                                    text = "Aucun paramètre ne correspond aux filtres actifs.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Button(
                                    onClick = {
                                        viewModel.updateSearchQuery("")
                                        viewModel.selectSubSection(SubSection.ALL)
                                        viewModel.selectStatusFilter(EntryStatusFilter.ALL)
                                    },
                                    modifier = Modifier.testTag("reset_filters_button")
                                ) {
                                    Text("Réinitialiser les filtres")
                                }
                            }
                        }
                    } else {
                        val previousSlotIdx = if (uiState.selectedSlotIndex > 1) uiState.selectedSlotIndex - 1 else null
                        val previousSlotLbl = previousSlotIdx?.let { uiState.shiftMetadata.getSlotLabel(it) }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("parameters_lazy_list"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                items = uiState.filteredParameters,
                                key = { _, param -> param.id }
                            ) { index, parameter ->
                                val showPart1Header = index == 0 &&
                                    parameter.partNumber == 1 &&
                                    uiState.selectedSubSection == SubSection.ALL &&
                                    uiState.searchQuery.isBlank()

                                val showPart2Header = parameter.partNumber == 2 &&
                                    (index == 0 || uiState.filteredParameters[index - 1].partNumber == 1) &&
                                    uiState.selectedSubSection == SubSection.ALL &&
                                    uiState.searchQuery.isBlank()

                                if (showPart1Header) {
                                    PartSectionDividerCard(
                                        partNumber = 1,
                                        subtitle = "Déméthaniseur, Dééthaniseur, Dépropaniseur, Débutaniseur & Réinjection",
                                        parameterCount = 35
                                    )
                                } else if (showPart2Header) {
                                    PartSectionDividerCard(
                                        partNumber = 2,
                                        subtitle = "Suite Réinjection, Déisopentaniseur & Pompes Gasoline",
                                        parameterCount = 12
                                    )
                                }

                                val currentReading = uiState.readingsByParamAndSlot[parameter.id to uiState.selectedSlotIndex]
                                val prevReading = previousSlotIdx?.let { prevIdx ->
                                    uiState.readingsByParamAndSlot[parameter.id to prevIdx]
                                }
                                val requester = focusRequesters[parameter.id] ?: remember { FocusRequester() }
                                val isLast = index == uiState.filteredParameters.lastIndex

                                ParameterEntryCard(
                                    parameter = parameter,
                                    reading = currentReading,
                                    previousSlotReading = prevReading,
                                    previousSlotLabel = previousSlotLbl,
                                    focusRequester = requester,
                                    isLastItem = isLast,
                                    onValueChanged = { newVal, isStopped ->
                                        viewModel.updateReadingValue(
                                            parameterId = parameter.id,
                                            rawValue = newVal,
                                            isStoppedOrNa = isStopped
                                        )
                                    },
                                    onMoveToNext = {
                                        if (!isLast) {
                                            val nextParam = uiState.filteredParameters[index + 1]
                                            coroutineScope.launch {
                                                listState.animateScrollToItem((index + 1).coerceAtMost(uiState.filteredParameters.lastIndex))
                                                runCatching {
                                                    focusRequesters[nextParam.id]?.requestFocus()
                                                }
                                            }
                                        } else {
                                            focusManager.clearFocus()
                                        }
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }

                MainDestination.SUMMARY_TABLE_24H -> {
                    Summary24HScreen(
                        metadata = uiState.shiftMetadata,
                        rows24H = uiState.rows24H,
                        slotSummaries = uiState.slotSummaries,
                        selectedSubSection = uiState.selectedSubSection,
                        onSelectSubSection = { viewModel.selectSubSection(it) },
                        onCellClick = { param, slotIdx, reading ->
                            editingCellTarget = Triple(param, slotIdx, reading)
                        },
                        onExportCsv = {
                            viewModel.exportAndShareCsv(context)
                        },
                        onExportPdf = {
                            viewModel.exportAndSharePdf(context)
                        },
                        onOpenPreviewDialog = {
                            showExportModal = true
                        }
                    )
                }
            }
        }
    }

    // Modal 1: Date Picker
    if (showDatePicker) {
        ShiftDatePickerModal(
            currentDateIso = uiState.selectedDateIso,
            loggedDates = uiState.loggedDates,
            onSelectDate = { viewModel.selectDate(it) },
            onDismiss = { showDatePicker = false }
        )
    }

    // Modal 2: Edit Time Slot Label (08H00, 14H00, 20H00, 02H00)
    editingSlotIndex?.let { slotIdx ->
        EditSlotTimeLabelDialog(
            slotIndex = slotIdx,
            currentLabel = uiState.shiftMetadata.getSlotLabel(slotIdx),
            onConfirm = { newLabel ->
                viewModel.updateSlotTimeLabel(slotIdx, newLabel)
            },
            onDismiss = { editingSlotIndex = null }
        )
    }

    // Modal 3: Quick Edit Cell from 24H Table
    editingCellTarget?.let { (param, slotIdx, reading) ->
        QuickEditCellModal(
            parameter = param,
            slotIndex = slotIdx,
            slotLabel = uiState.shiftMetadata.getSlotLabel(slotIdx),
            currentReading = reading,
            onSave = { valStr, isStopped ->
                viewModel.updateReadingAtSlot(param.id, slotIdx, valStr, isStopped)
            },
            onDismiss = { editingCellTarget = null }
        )
    }

    // Modal 4: Export & Preview (CSV Excel / PDF 24H / Plain Text Summary)
    if (showExportModal) {
        val allRows = remember(uiState.allParameters, uiState.readingsByParamAndSlot) {
            ReportExporter.buildRows(
                uiState.allParameters,
                uiState.readingsByParamAndSlot.values.toList()
            )
        }
        val textSummary = remember(uiState.shiftMetadata, allRows) {
            ReportExporter.generateTextSummary(uiState.shiftMetadata, allRows)
        }
        val csvPreview = remember(uiState.shiftMetadata, allRows) {
            ReportExporter.generateCsvContent(uiState.shiftMetadata, allRows)
        }

        ReportPreviewAndShareModal(
            dateIso = uiState.selectedDateIso,
            textSummary = textSummary,
            csvPreview = csvPreview,
            onCopySummary = {
                ReportExporter.copyToClipboard(
                    context = context,
                    label = "Relevés 24H ${uiState.selectedDateIso}",
                    text = textSummary
                )
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Résumé 24H copié dans le presse-papiers !")
                }
            },
            onShareText = {
                ReportExporter.sharePlainText(
                    context = context,
                    subject = "Relevés 24H Colonnes & Ballons - ${uiState.selectedDateIso}",
                    bodyText = textSummary
                )
            },
            onExportCsv = {
                viewModel.exportAndShareCsv(context)
            },
            onExportPdf = {
                viewModel.exportAndSharePdf(context)
            },
            onDismiss = { showExportModal = false }
        )
    }
}
