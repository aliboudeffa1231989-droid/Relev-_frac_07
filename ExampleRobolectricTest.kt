package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.PlantParametersCatalog
import com.example.data.local.DailyShiftMetadataEntity
import com.example.data.local.ParameterReadingEntity
import com.example.util.ReportExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read app name and verify catalog and 24h csv export`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PetroLog 24H", appName)

        val params = PlantParametersCatalog.parameters
        assertEquals(47, params.size)
        assertEquals("07-LG1017", params.first().tag)
        assertEquals("07-PG1574", params.last().tag)

        val metadata = DailyShiftMetadataEntity(
            dateIso = "2026-10-30",
            dayOperatorName = "K. Mansouri",
            dayOperatorSignature = "KM-08",
            nightOperatorName = "S. Benali",
            nightOperatorSignature = "SB-20"
        )
        val sampleReadings = listOf(
            ParameterReadingEntity("2026-10-30", 1, 1, "54.2", false),
            ParameterReadingEntity("2026-10-30", 2, 1, "", true)
        )
        val rows = ReportExporter.buildRows(params, sampleReadings)
        val csv = ReportExporter.generateCsvContent(metadata, rows)
        assertTrue(csv.contains("07-LG1017"))
        assertTrue(csv.contains("54.2"))
        assertTrue(csv.contains("ARRÊT"))
        assertTrue(csv.contains("K. Mansouri"))
    }

    @Test
    fun `main activity launches and renders 4 time slots and 24h summary tab without crash`() {
        composeTestRule.onNodeWithTag("slot_tab_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_tab_2").performClick()
        composeTestRule.onNodeWithTag("nav_summary_24h").performClick()
        composeTestRule.onNodeWithTag("export_csv_button").assertIsDisplayed()
    }
}
