package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PetroLogRepository
import com.example.data.PlantParameter
import com.example.data.PlantParametersCatalog
import com.example.data.SubSection
import com.example.data.local.DailyShiftMetadataEntity
import com.example.data.local.ParameterReadingEntity
import com.example.util.Parameter24HRow
import com.example.util.ReportExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class EntryStatusFilter(val label: String) {
    ALL("Tous"),
    REMAINING("À saisir"),
    FILLED("Saisis"),
    STOPPED_NA("En arrêt / N/A")
}

enum class MainDestination {
    FIELD_ENTRY,
    SUMMARY_TABLE_24H
}

data class SlotProgressSummary(
    val slotIndex: Int,
    val label: String,
    val completedCount: Int,
    val stoppedCount: Int,
    val totalCount: Int
) {
    val fraction: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
}

data class PetroLogUiState(
    val selectedDateIso: String = PetroLogViewModel.todayIso(),
    val selectedSlotIndex: Int = 1,
    val activeDestination: MainDestination = MainDestination.FIELD_ENTRY,
    val searchQuery: String = "",
    val selectedSubSection: SubSection = SubSection.ALL,
    val selectedStatusFilter: EntryStatusFilter = EntryStatusFilter.ALL,
    val shiftMetadata: DailyShiftMetadataEntity = DailyShiftMetadataEntity(dateIso = PetroLogViewModel.todayIso()),
    val readingsByParamAndSlot: Map<Pair<Int, Int>, ParameterReadingEntity> = emptyMap(),
    val filteredParameters: List<PlantParameter> = PlantParametersCatalog.parameters,
    val allParameters: List<PlantParameter> = PlantParametersCatalog.parameters,
    val slotSummaries: List<SlotProgressSummary> = PlantParametersCatalog.DEFAULT_SLOTS.map { (idx, lbl) ->
        SlotProgressSummary(
            slotIndex = idx,
            label = lbl,
            completedCount = 0,
            stoppedCount = 0,
            totalCount = PlantParametersCatalog.parameters.size
        )
    },
    val rows24H: List<Parameter24HRow> = ReportExporter.buildRows(
        PlantParametersCatalog.parameters,
        emptyList()
    ),
    val loggedDates: List<String> = emptyList(),
    val lastSavedTimestamp: Long? = null
) {
    val currentSlotSummary: SlotProgressSummary
        get() = slotSummaries.find { it.slotIndex == selectedSlotIndex }
            ?: SlotProgressSummary(
                slotIndex = selectedSlotIndex,
                label = shiftMetadata.getSlotLabel(selectedSlotIndex),
                completedCount = 0,
                stoppedCount = 0,
                totalCount = allParameters.size
            )
}

private data class FilterState(
    val dateIso: String,
    val slotIndex: Int,
    val destination: MainDestination,
    val searchQuery: String,
    val subSection: SubSection,
    val statusFilter: EntryStatusFilter
)

@OptIn(ExperimentalCoroutinesApi::class)
class PetroLogViewModel(
    private val repository: PetroLogRepository
) : ViewModel() {

    private val _selectedDateIso = MutableStateFlow(todayIso())
    private val _selectedSlotIndex = MutableStateFlow(1)
    private val _activeDestination = MutableStateFlow(MainDestination.FIELD_ENTRY)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedSubSection = MutableStateFlow(SubSection.ALL)
    private val _selectedStatusFilter = MutableStateFlow(EntryStatusFilter.ALL)
    private val _lastSavedTimestamp = MutableStateFlow<Long?>(null)

    private val _isDarkTheme = MutableStateFlow(false) // Default bright light theme across all backgrounds
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val filterFlow = combine(
        _selectedDateIso,
        _selectedSlotIndex,
        _activeDestination,
        _searchQuery,
        _selectedSubSection
    ) { dateIso, slotIndex, destination, query, subSection ->
        FilterState(
            dateIso = dateIso,
            slotIndex = slotIndex,
            destination = destination,
            searchQuery = query,
            subSection = subSection,
            statusFilter = EntryStatusFilter.ALL
        )
    }.combine(_selectedStatusFilter) { base, status ->
        base.copy(statusFilter = status)
    }

    private val dateDataFlow = _selectedDateIso.flatMapLatest { dateIso ->
        combine(
            repository.observeShiftMetadata(dateIso),
            repository.observeReadingsForDate(dateIso),
            repository.observeLoggedDates()
        ) { metadata, readings, loggedDates ->
            Triple(
                metadata ?: DailyShiftMetadataEntity(dateIso = dateIso),
                readings,
                loggedDates
            )
        }
    }

    val uiState: StateFlow<PetroLogUiState> = combine(
        filterFlow,
        dateDataFlow,
        _lastSavedTimestamp
    ) { filter, (metadata, readings, loggedDates), lastSaved ->
        val allParams = PlantParametersCatalog.parameters
        val readingsMap = readings.associateBy { it.parameterId to it.slotIndex }

        val slotSummaries = (1..4).map { slotIdx ->
            val slotReadings = allParams.mapNotNull { p -> readingsMap[p.id to slotIdx] }
            SlotProgressSummary(
                slotIndex = slotIdx,
                label = metadata.getSlotLabel(slotIdx),
                completedCount = slotReadings.count { it.isCompleted },
                stoppedCount = slotReadings.count { it.isStoppedOrNa },
                totalCount = allParams.size
            )
        }

        val q = filter.searchQuery.trim().lowercase(Locale.ROOT)
        val filtered = allParams.filter { param ->
            val matchesSubSection = filter.subSection == SubSection.ALL || param.subSection == filter.subSection
            val matchesQuery = q.isEmpty() ||
                param.tag.lowercase(Locale.ROOT).contains(q) ||
                param.name.lowercase(Locale.ROOT).contains(q) ||
                param.number.toString() == q ||
                param.subSection.label.lowercase(Locale.ROOT).contains(q)

            val reading = readingsMap[param.id to filter.slotIndex]
            val isCompleted = reading?.isCompleted == true
            val isStopped = reading?.isStoppedOrNa == true

            val matchesStatus = when (filter.statusFilter) {
                EntryStatusFilter.ALL -> true
                EntryStatusFilter.REMAINING -> !isCompleted
                EntryStatusFilter.FILLED -> isCompleted
                EntryStatusFilter.STOPPED_NA -> isStopped
            }
            matchesSubSection && matchesQuery && matchesStatus
        }

        val rows24H = ReportExporter.buildRows(filtered, readings)

        PetroLogUiState(
            selectedDateIso = filter.dateIso,
            selectedSlotIndex = filter.slotIndex,
            activeDestination = filter.destination,
            searchQuery = filter.searchQuery,
            selectedSubSection = filter.subSection,
            selectedStatusFilter = filter.statusFilter,
            shiftMetadata = metadata,
            readingsByParamAndSlot = readingsMap,
            filteredParameters = filtered,
            allParameters = allParams,
            slotSummaries = slotSummaries,
            rows24H = rows24H,
            loggedDates = loggedDates,
            lastSavedTimestamp = lastSaved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetroLogUiState()
    )

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun selectDate(dateIso: String) {
        _selectedDateIso.value = dateIso
    }

    fun stepDateByDays(deltaDays: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val current = runCatching { sdf.parse(_selectedDateIso.value) }.getOrNull() ?: Date()
        val cal = Calendar.getInstance().apply {
            time = current
            add(Calendar.DAY_OF_YEAR, deltaDays)
        }
        _selectedDateIso.value = sdf.format(cal.time)
    }

    fun selectSlot(slotIndex: Int) {
        _selectedSlotIndex.value = slotIndex.coerceIn(1, 4)
    }

    fun selectDestination(destination: MainDestination) {
        _activeDestination.value = destination
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSubSection(subSection: SubSection) {
        _selectedSubSection.value = subSection
    }

    fun selectStatusFilter(filter: EntryStatusFilter) {
        _selectedStatusFilter.value = filter
    }

    fun updateReadingValue(parameterId: Int, rawValue: String, isStoppedOrNa: Boolean) {
        val dateIso = _selectedDateIso.value
        val slotIndex = _selectedSlotIndex.value
        viewModelScope.launch {
            repository.saveReadingValue(
                dateIso = dateIso,
                parameterId = parameterId,
                slotIndex = slotIndex,
                rawValue = rawValue,
                isStoppedOrNa = isStoppedOrNa
            )
            _lastSavedTimestamp.value = System.currentTimeMillis()
        }
    }

    fun updateReadingAtSlot(parameterId: Int, slotIndex: Int, rawValue: String, isStoppedOrNa: Boolean) {
        val dateIso = _selectedDateIso.value
        viewModelScope.launch {
            repository.saveReadingValue(
                dateIso = dateIso,
                parameterId = parameterId,
                slotIndex = slotIndex,
                rawValue = rawValue,
                isStoppedOrNa = isStoppedOrNa
            )
            _lastSavedTimestamp.value = System.currentTimeMillis()
        }
    }

    fun updateShiftOperators(
        dayOperatorName: String,
        dayOperatorSignature: String,
        nightOperatorName: String,
        nightOperatorSignature: String,
        shiftObservations: String
    ) {
        val dateIso = _selectedDateIso.value
        viewModelScope.launch {
            repository.updateShiftOperators(
                dateIso = dateIso,
                dayOperatorName = dayOperatorName,
                dayOperatorSignature = dayOperatorSignature,
                nightOperatorName = nightOperatorName,
                nightOperatorSignature = nightOperatorSignature,
                shiftObservations = shiftObservations
            )
            _lastSavedTimestamp.value = System.currentTimeMillis()
        }
    }

    fun updateSlotTimeLabel(slotIndex: Int, newLabel: String) {
        val dateIso = _selectedDateIso.value
        viewModelScope.launch {
            repository.updateSlotLabel(
                dateIso = dateIso,
                slotIndex = slotIndex,
                newLabel = newLabel
            )
            _lastSavedTimestamp.value = System.currentTimeMillis()
        }
    }

    fun copyFromPreviousSlot() {
        val currentSlot = _selectedSlotIndex.value
        if (currentSlot <= 1) return
        val dateIso = _selectedDateIso.value
        viewModelScope.launch {
            repository.copyPreviousSlotToCurrent(
                dateIso = dateIso,
                fromSlotIndex = currentSlot - 1,
                toSlotIndex = currentSlot
            )
            _lastSavedTimestamp.value = System.currentTimeMillis()
        }
    }

    fun clearCurrentSlot() {
        val dateIso = _selectedDateIso.value
        val slotIndex = _selectedSlotIndex.value
        viewModelScope.launch {
            repository.clearSlotReadings(dateIso, slotIndex)
            _lastSavedTimestamp.value = System.currentTimeMillis()
        }
    }

    fun exportAndShareCsv(context: Context) {
        val state = uiState.value
        val allRows = ReportExporter.buildRows(
            state.allParameters,
            state.readingsByParamAndSlot.values.toList()
        )
        val file = ReportExporter.exportCsvFile(context, state.shiftMetadata, allRows)
        ReportExporter.shareFile(
            context = context,
            file = file,
            mimeType = "text/csv",
            chooserTitle = "Exporter la feuille 24H (.csv Excel)",
            subject = "Relevés 24H Colonnes & Ballons - ${state.selectedDateIso}",
            bodyText = "Veuillez trouver ci-joint la feuille de relevés 24H (CSV compatible Excel) du ${state.selectedDateIso} pour la section COLONNES, BALLONS, REBOUILLEURS & POMPES."
        )
    }

    fun exportAndSharePdf(context: Context) {
        val state = uiState.value
        val allRows = ReportExporter.buildRows(
            state.allParameters,
            state.readingsByParamAndSlot.values.toList()
        )
        val file = ReportExporter.exportPdfFile(context, state.shiftMetadata, allRows)
        ReportExporter.shareFile(
            context = context,
            file = file,
            mimeType = "application/pdf",
            chooserTitle = "Partager le rapport PDF 24H",
            subject = "Rapport PDF Relevés 24H - ${state.selectedDateIso}",
            bodyText = "Rapport quotidien des relevés 24H (Colonnes, Ballons, Rebouilleurs & Pompes) du ${state.selectedDateIso}.\n" +
                "Quart de Jour : ${state.shiftMetadata.dayOperatorName.ifBlank { "Non renseigné" }}\n" +
                "Quart de Nuit : ${state.shiftMetadata.nightOperatorName.ifBlank { "Non renseigné" }}"
        )
    }

    companion object {
        fun todayIso(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun formatDisplayDate(dateIso: String): String {
            return runCatching {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formatter = SimpleDateFormat("EEE dd MMM yyyy", Locale.FRENCH)
                val parsed = parser.parse(dateIso) ?: return dateIso
                formatter.format(parsed).replaceFirstChar { it.uppercase() }
            }.getOrDefault(dateIso)
        }
    }
}

class PetroLogViewModelFactory(
    private val repository: PetroLogRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetroLogViewModel::class.java)) {
            return PetroLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
