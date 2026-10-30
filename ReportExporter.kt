package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.PlantParameter
import com.example.data.local.DailyShiftMetadataEntity
import com.example.data.local.ParameterReadingEntity
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class Parameter24HRow(
    val parameter: PlantParameter,
    val slot1: ParameterReadingEntity?,
    val slot2: ParameterReadingEntity?,
    val slot3: ParameterReadingEntity?,
    val slot4: ParameterReadingEntity?
) {
    fun getSlotText(slotIndex: Int): String {
        val reading = when (slotIndex) {
            1 -> slot1
            2 -> slot2
            3 -> slot3
            4 -> slot4
            else -> null
        }
        return reading?.displayTableValue ?: "—"
    }

    private fun numericValues(): List<Double> {
        return listOfNotNull(slot1, slot2, slot3, slot4)
            .filter { !it.isStoppedOrNa && it.rawValue.isNotBlank() }
            .mapNotNull { it.rawValue.replace(',', '.').toDoubleOrNull() }
    }

    val averageFormatted: String
        get() {
            val nums = numericValues()
            if (nums.isEmpty()) return "—"
            val avg = nums.average()
            return String.format(Locale.US, "%.1f", avg)
        }

    val deltaFormatted: String
        get() {
            val nums = numericValues()
            if (nums.size < 2) return "—"
            val diff = nums.last() - nums.first()
            val sign = if (diff > 0) "+" else ""
            return String.format(Locale.US, "%s%.1f", sign, diff)
        }
}

object ReportExporter {

    fun buildRows(
        parameters: List<PlantParameter>,
        readings: List<ParameterReadingEntity>
    ): List<Parameter24HRow> {
        val byParamAndSlot = readings.associateBy { it.parameterId to it.slotIndex }
        return parameters.map { param ->
            Parameter24HRow(
                parameter = param,
                slot1 = byParamAndSlot[param.id to 1],
                slot2 = byParamAndSlot[param.id to 2],
                slot3 = byParamAndSlot[param.id to 3],
                slot4 = byParamAndSlot[param.id to 4]
            )
        }
    }

    fun generateCsvContent(
        metadata: DailyShiftMetadataEntity,
        rows: List<Parameter24HRow>
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for direct Microsoft Excel compatibility
        sb.append('\uFEFF')
        sb.appendLine("SECTION;COLONNES, BALLONS, REBOUILLEURS & POMPES")
        sb.appendLine("DATE;${metadata.dateIso}")
        sb.appendLine(
            "QUART DE JOUR;${escapeCsv(metadata.dayOperatorName.ifBlank { "Non renseigné" })};" +
                "SIGNATURE JOUR;${escapeCsv(metadata.dayOperatorSignature.ifBlank { "Non signé" })}"
        )
        sb.appendLine(
            "QUART DE NUIT;${escapeCsv(metadata.nightOperatorName.ifBlank { "Non renseigné" })};" +
                "SIGNATURE NUIT;${escapeCsv(metadata.nightOperatorSignature.ifBlank { "Non signé" })}"
        )
        if (metadata.shiftObservations.isNotBlank()) {
            sb.appendLine("OBSERVATIONS;${escapeCsv(metadata.shiftObservations)}")
        }
        sb.appendLine()
        sb.appendLine(
            "N°;Partie;Sous-Section;Équipement;TAG;Unité;" +
                "Relevé 1 (${metadata.getSlotLabel(1)});" +
                "Relevé 2 (${metadata.getSlotLabel(2)});" +
                "Relevé 3 (${metadata.getSlotLabel(3)});" +
                "Relevé 4 (${metadata.getSlotLabel(4)});" +
                "Moyenne 24H;Écart (Δ)"
        )
        for (row in rows) {
            val p = row.parameter
            sb.appendLine(
                "${p.number};Partie ${p.partNumber};${escapeCsv(p.subSection.label)};" +
                    "${escapeCsv(p.name)};${p.tag};${p.measureType.unit};" +
                    "${escapeCsv(row.getSlotText(1))};" +
                    "${escapeCsv(row.getSlotText(2))};" +
                    "${escapeCsv(row.getSlotText(3))};" +
                    "${escapeCsv(row.getSlotText(4))};" +
                    "${row.averageFormatted};${row.deltaFormatted}"
            )
        }
        return sb.toString()
    }

    fun generateTextSummary(
        metadata: DailyShiftMetadataEntity,
        rows: List<Parameter24HRow>
    ): String {
        val total = rows.size
        val count1 = rows.count { it.slot1?.isCompleted == true }
        val count2 = rows.count { it.slot2?.isCompleted == true }
        val count3 = rows.count { it.slot3?.isCompleted == true }
        val count4 = rows.count { it.slot4?.isCompleted == true }

        val sb = StringBuilder()
        sb.appendLine("═══════════════════════════════════════════════════")
        sb.appendLine("RAPPORT QUOTIDIEN 24H — COLONNES, BALLONS, REBOUILLEURS & POMPES")
        sb.appendLine("Date : ${metadata.dateIso}")
        sb.appendLine("Quart de Jour : ${metadata.dayOperatorName.ifBlank { "—" }} (Visa: ${metadata.dayOperatorSignature.ifBlank { "—" }})")
        sb.appendLine("Quart de Nuit : ${metadata.nightOperatorName.ifBlank { "—" }} (Visa: ${metadata.nightOperatorSignature.ifBlank { "—" }})")
        if (metadata.shiftObservations.isNotBlank()) {
            sb.appendLine("Consignes / Observations : ${metadata.shiftObservations}")
        }
        sb.appendLine(
            "Progression : ${metadata.getSlotLabel(1)} ($count1/$total) | " +
                "${metadata.getSlotLabel(2)} ($count2/$total) | " +
                "${metadata.getSlotLabel(3)} ($count3/$total) | " +
                "${metadata.getSlotLabel(4)} ($count4/$total)"
        )
        sb.appendLine("═══════════════════════════════════════════════════")
        sb.appendLine(
            String.format(
                Locale.US,
                "%-3s | %-10s | %-34s | %-7s | %-7s | %-7s | %-7s",
                "N°", "TAG", "ÉQUIPEMENT",
                metadata.getSlotLabel(1),
                metadata.getSlotLabel(2),
                metadata.getSlotLabel(3),
                metadata.getSlotLabel(4)
            )
        )
        sb.appendLine("───────────────────────────────────────────────────")
        for (row in rows) {
            val p = row.parameter
            sb.appendLine(
                String.format(
                    Locale.US,
                    "%02d  | %-10s | %-34s | %-7s | %-7s | %-7s | %-7s (%s)",
                    p.number,
                    p.tag,
                    p.name.take(34),
                    row.getSlotText(1),
                    row.getSlotText(2),
                    row.getSlotText(3),
                    row.getSlotText(4),
                    p.measureType.unit
                )
            )
        }
        return sb.toString()
    }

    fun exportCsvFile(
        context: Context,
        metadata: DailyShiftMetadataEntity,
        rows: List<Parameter24HRow>
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "Releves_24H_Colonnes_${metadata.dateIso}.csv")
        val content = generateCsvContent(metadata, rows)
        FileOutputStream(file).use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun exportPdfFile(
        context: Context,
        metadata: DailyShiftMetadataEntity,
        rows: List<Parameter24HRow>
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "Rapport_24H_Colonnes_${metadata.dateIso}.pdf")

        val pdfDocument = PdfDocument()
        // A4 Portrait in points: 595 x 842
        val pageWidth = 595
        val pageHeight = 842

        // Split 47 rows into Page 1 (Partie 1: 35 rows) and Page 2 (Partie 2: 12 rows + signature blocks)
        val page1Rows = rows.take(26)
        val page2Rows = rows.drop(26)

        val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(page1Info)
        drawPdfPage(
            canvas = page1.canvas,
            metadata = metadata,
            rows = page1Rows,
            allRows = rows,
            pageNumber = 1,
            totalPages = 2,
            isLastPage = false
        )
        pdfDocument.finishPage(page1)

        val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = pdfDocument.startPage(page2Info)
        drawPdfPage(
            canvas = page2.canvas,
            metadata = metadata,
            rows = page2Rows,
            allRows = rows,
            pageNumber = 2,
            totalPages = 2,
            isLastPage = true
        )
        pdfDocument.finishPage(page2)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    private fun drawPdfPage(
        canvas: Canvas,
        metadata: DailyShiftMetadataEntity,
        rows: List<Parameter24HRow>,
        allRows: List<Parameter24HRow>,
        pageNumber: Int,
        totalPages: Int,
        isLastPage: Boolean
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 0.8f
        }

        // Top Industrial Banner
        bgPaint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(RectF(24f, 20f, 571f, 76f), 6f, 6f, bgPaint)

        textPaint.color = Color.parseColor("#F59E0B")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 9f
        canvas.drawText("FEUILLE DE RELEVÉS OPÉRATEUR 24 HEURES • UNITÉ DE FRACTIONNEMENT", 36f, 36f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 12.5f
        canvas.drawText("SECTION : COLONNES, BALLONS, REBOUILLEURS & POMPES", 36f, 54f, textPaint)

        textPaint.color = Color.parseColor("#94A3B8")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 8.5f
        canvas.drawText("Date : ${metadata.dateIso}   |   Page $pageNumber / $totalPages", 36f, 68f, textPaint)

        // Shift Metadata Box
        bgPaint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(RectF(24f, 82f, 571f, 124f), 4f, 4f, bgPaint)
        canvas.drawRoundRect(RectF(24f, 82f, 571f, 124f), 4f, 4f, linePaint.apply { style = Paint.Style.STROKE })
        linePaint.style = Paint.Style.FILL

        textPaint.color = Color.parseColor("#334155")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 8.5f
        canvas.drawText(
            "QUART DE JOUR (08H00 - 20H00) : ${metadata.dayOperatorName.ifBlank { "—" }}",
            34f, 96f, textPaint
        )
        canvas.drawText(
            "Visa / Signature Jour : ${metadata.dayOperatorSignature.ifBlank { "—" }}",
            34f, 112f, textPaint
        )

        canvas.drawText(
            "QUART DE NUIT (20H00 - 08H00) : ${metadata.nightOperatorName.ifBlank { "—" }}",
            305f, 96f, textPaint
        )
        canvas.drawText(
            "Visa / Signature Nuit : ${metadata.nightOperatorSignature.ifBlank { "—" }}",
            305f, 112f, textPaint
        )

        // Table Header
        var currentY = 134f
        val rowHeight = 22f
        bgPaint.color = Color.parseColor("#1E293B")
        canvas.drawRect(24f, currentY, 571f, currentY + rowHeight, bgPaint)

        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 8f

        val colNumX = 30f
        val colNameX = 54f
        val colTagX = 258f
        val colUnitX = 324f
        val col1X = 358f
        val col2X = 412f
        val col3X = 466f
        val col4X = 520f

        val headerBaseline = currentY + 14.5f
        canvas.drawText("N°", colNumX, headerBaseline, textPaint)
        canvas.drawText("ÉQUIPEMENT / PARAMÈTRE", colNameX, headerBaseline, textPaint)
        canvas.drawText("TAG", colTagX, headerBaseline, textPaint)
        canvas.drawText("UNITÉ", colUnitX, headerBaseline, textPaint)
        canvas.drawText(metadata.getSlotLabel(1), col1X, headerBaseline, textPaint)
        canvas.drawText(metadata.getSlotLabel(2), col2X, headerBaseline, textPaint)
        canvas.drawText(metadata.getSlotLabel(3), col3X, headerBaseline, textPaint)
        canvas.drawText(metadata.getSlotLabel(4), col4X, headerBaseline, textPaint)

        currentY += rowHeight

        // Draw Rows
        rows.forEachIndexed { index, row ->
            val p = row.parameter
            bgPaint.color = if (index % 2 == 0) Color.WHITE else Color.parseColor("#F1F5F9")
            canvas.drawRect(24f, currentY, 571f, currentY + rowHeight, bgPaint)
            canvas.drawLine(24f, currentY + rowHeight, 571f, currentY + rowHeight, linePaint)

            val baseline = currentY + 14.5f

            // Number
            monoPaint.color = Color.parseColor("#64748B")
            monoPaint.textSize = 8f
            canvas.drawText(String.format(Locale.US, "%02d", p.number), colNumX, baseline, monoPaint)

            // Equipment Name
            textPaint.color = Color.parseColor("#0F172A")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.textSize = 8f
            canvas.drawText(p.name.take(38), colNameX, baseline, textPaint)

            // TAG in Bold Monospace
            monoPaint.color = Color.parseColor("#0F172A")
            monoPaint.textSize = 8.2f
            canvas.drawText(p.tag, colTagX, baseline, monoPaint)

            // Unit
            textPaint.color = Color.parseColor("#64748B")
            textPaint.textSize = 7.5f
            canvas.drawText(p.measureType.unit, colUnitX, baseline, textPaint)

            // 4 Slot Values
            val slotXs = listOf(col1X, col2X, col3X, col4X)
            for (slotIdx in 1..4) {
                val valStr = row.getSlotText(slotIdx)
                monoPaint.textSize = 8f
                monoPaint.color = when (valStr) {
                    "ARRÊT" -> Color.parseColor("#D97706")
                    "—" -> Color.parseColor("#94A3B8")
                    else -> Color.parseColor("#0F172A")
                }
                canvas.drawText(valStr, slotXs[slotIdx - 1], baseline, monoPaint)
            }

            currentY += rowHeight
        }

        // On last page, draw completion summary & formal signature blocks
        if (isLastPage) {
            currentY += 16f
            bgPaint.color = Color.parseColor("#F8FAFC")
            canvas.drawRoundRect(RectF(24f, currentY, 571f, currentY + 90f), 6f, 6f, bgPaint)
            linePaint.style = Paint.Style.STROKE
            canvas.drawRoundRect(RectF(24f, currentY, 571f, currentY + 90f), 6f, 6f, linePaint)
            linePaint.style = Paint.Style.FILL

            textPaint.color = Color.parseColor("#0F172A")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 9f
            canvas.drawText("VALIDATION & VISAS DE FIN DE POSTE (24 HEURES)", 36f, currentY + 18f, textPaint)

            val total = allRows.size
            val c1 = allRows.count { it.slot1?.isCompleted == true }
            val c2 = allRows.count { it.slot2?.isCompleted == true }
            val c3 = allRows.count { it.slot3?.isCompleted == true }
            val c4 = allRows.count { it.slot4?.isCompleted == true }

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.textSize = 8f
            canvas.drawText(
                "Taux de remplissage :  ${metadata.getSlotLabel(1)}: $c1/$total   |   " +
                    "${metadata.getSlotLabel(2)}: $c2/$total   |   " +
                    "${metadata.getSlotLabel(3)}: $c3/$total   |   " +
                    "${metadata.getSlotLabel(4)}: $c4/$total",
                36f,
                currentY + 36f,
                textPaint
            )

            if (metadata.shiftObservations.isNotBlank()) {
                canvas.drawText(
                    "Observations : ${metadata.shiftObservations.take(95)}",
                    36f,
                    currentY + 54f,
                    textPaint
                )
            }

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText(
                "Signature Quart de Jour : ${metadata.dayOperatorSignature.ifBlank { "____________________" }}",
                36f,
                currentY + 76f,
                textPaint
            )
            canvas.drawText(
                "Signature Quart de Nuit : ${metadata.nightOperatorSignature.ifBlank { "____________________" }}",
                310f,
                currentY + 76f,
                textPaint
            )
        }
    }

    fun shareFile(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String,
        subject: String,
        bodyText: String
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bodyText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun sharePlainText(
        context: Context,
        subject: String,
        bodyText: String
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bodyText)
        }
        val chooser = Intent.createChooser(intent, "Partager le résumé 24H").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun escapeCsv(input: String): String {
        val cleaned = input.replace("\n", " ").replace("\r", " ").trim()
        return if (cleaned.contains(";") || cleaned.contains("\"") || cleaned.contains(",")) {
            "\"${cleaned.replace("\"", "\"\"")}\""
        } else {
            cleaned
        }
    }
}
