package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class FileCategory(val label: String) {
    DOCUMENT("Document"),
    SPREADSHEET("Spreadsheet"),
    PRESENTATION("Presentation"),
    CODE("Code & Dev"),
    ARCHIVE("Archive"),
    IMAGE("Image")
}

data class GeneratedFileInfo(
    val fileName: String,
    val extension: String,
    val mimeType: String,
    val sizeBytes: Long,
    val localUri: String? = null,
    val localFilePath: String? = null,
    val category: FileCategory = FileCategory.DOCUMENT,
    val contentPreview: String? = null
) {
    val formattedSize: String
        get() {
            return when {
                sizeBytes < 1024 -> "$sizeBytes B"
                sizeBytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", sizeBytes / 1024f)
                else -> String.format(Locale.US, "%.1f MB", sizeBytes / (1024f * 1024f))
            }
        }
}

object UniversalFileGenerator {

    /**
     * Determines whether the user query specifically asks to generate/download/export a file.
     */
    fun detectFileRequest(prompt: String): Pair<String, String>? {
        val lower = prompt.lowercase(Locale.US)
        return when {
            // PDF
            lower.contains("pdf") || lower.contains("as a pdf") || lower.contains("make a pdf") || lower.contains("create a pdf") ->
                "pdf" to "application/pdf"

            // Word DOCX
            lower.contains("word doc") || lower.contains("word document") || lower.contains("docx") || lower.contains("doc file") ->
                "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

            // Excel & Spreadsheets
            lower.contains("excel") || lower.contains("spreadsheet") || lower.contains("xlsx") || lower.contains(".xlsx") ->
                "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

            // CSV
            lower.contains("csv") || lower.contains(".csv") ->
                "csv" to "text/csv"

            // PowerPoint
            lower.contains("powerpoint") || lower.contains("presentation") || lower.contains("pptx") || lower.contains("slides") ->
                "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation"

            // ZIP Archive
            lower.contains("zip") || lower.contains(".zip") || lower.contains("archive") || lower.contains("project folder") ->
                "zip" to "application/zip"

            // HTML Web
            lower.contains("html") || lower.contains("webpage") || lower.contains("website") ->
                "html" to "text/html"

            // Markdown
            lower.contains("markdown") || lower.contains(".md") ->
                "md" to "text/markdown"

            // Plain text
            lower.contains("txt") || lower.contains("text file") || lower.contains("save as txt") ->
                "txt" to "text/plain"

            // Programming languages
            lower.contains("python") || lower.contains(".py") -> "py" to "text/x-python"
            lower.contains("java") && !lower.contains("jamb") -> "java" to "text/x-java"
            lower.contains("javascript") || lower.contains(".js") -> "js" to "application/javascript"
            lower.contains("typescript") || lower.contains(".ts") -> "ts" to "application/typescript"
            lower.contains("json") || lower.contains(".json") -> "json" to "application/json"
            lower.contains("xml") || lower.contains(".xml") -> "xml" to "application/xml"
            lower.contains("svg") || lower.contains(".svg") -> "svg" to "image/svg+xml"
            lower.contains("sql") || lower.contains(".sql") -> "sql" to "application/sql"

            else -> null
        }
    }

    /**
     * Primary entry point: generates the real file matching the detected extension.
     */
    fun generateFileFromContent(
        context: Context,
        topic: String,
        content: String,
        targetExtension: String,
        subject: String = "StudyBuddy"
    ): GeneratedFileInfo {
        val sanitizedTopic = topic.trim()
            .replace(Regex("[^a-zA-Z0-9_ -]"), "")
            .replace(" ", "_")
            .take(30)
            .ifBlank { "study_document" }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val baseName = "${sanitizedTopic}_$timestamp"

        return when (targetExtension.lowercase(Locale.US)) {
            "pdf" -> {
                val file = createPdfDocument(context, baseName, topic, content, subject)
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "pdf",
                    mimeType = "application/pdf",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.DOCUMENT,
                    contentPreview = content.take(200)
                )
            }
            "docx" -> {
                val file = createDocxDocument(context, baseName, topic, content, subject)
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "docx",
                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.DOCUMENT,
                    contentPreview = content.take(200)
                )
            }
            "xlsx" -> {
                val file = createXlsxSpreadsheet(context, baseName, topic, content)
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "xlsx",
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.SPREADSHEET,
                    contentPreview = content.take(200)
                )
            }
            "csv" -> {
                val file = createCsvFile(context, baseName, topic, content)
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "csv",
                    mimeType = "text/csv",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.SPREADSHEET,
                    contentPreview = content.take(200)
                )
            }
            "pptx" -> {
                val file = createPptxPresentation(context, baseName, topic, content)
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "pptx",
                    mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.PRESENTATION,
                    contentPreview = content.take(200)
                )
            }
            "zip" -> {
                val file = createProjectZip(context, baseName, topic, content)
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "zip",
                    mimeType = "application/zip",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.ARCHIVE,
                    contentPreview = "Multi-file project archive ($topic)"
                )
            }
            "html" -> {
                val file = createHtmlDocument(context, baseName, topic, content)
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "html",
                    mimeType = "text/html",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.CODE,
                    contentPreview = content.take(200)
                )
            }
            "md" -> {
                val file = createTextFile(context, "$baseName.md", content, "text/markdown")
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "md",
                    mimeType = "text/markdown",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.DOCUMENT,
                    contentPreview = content.take(200)
                )
            }
            "py" -> {
                val file = createTextFile(context, "$baseName.py", content, "text/x-python")
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "py",
                    mimeType = "text/x-python",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.CODE,
                    contentPreview = content.take(200)
                )
            }
            "java" -> {
                val file = createTextFile(context, "$baseName.java", content, "text/x-java")
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "java",
                    mimeType = "text/x-java",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.CODE,
                    contentPreview = content.take(200)
                )
            }
            "json" -> {
                val file = createTextFile(context, "$baseName.json", content, "application/json")
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "json",
                    mimeType = "application/json",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.CODE,
                    contentPreview = content.take(200)
                )
            }
            "sql" -> {
                val file = createTextFile(context, "$baseName.sql", content, "application/sql")
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "sql",
                    mimeType = "application/sql",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.CODE,
                    contentPreview = content.take(200)
                )
            }
            else -> {
                // Default fallback: Plain Text
                val file = createTextFile(context, "$baseName.txt", content, "text/plain")
                GeneratedFileInfo(
                    fileName = file.name,
                    extension = "txt",
                    mimeType = "text/plain",
                    sizeBytes = file.length(),
                    localFilePath = file.absolutePath,
                    category = FileCategory.DOCUMENT,
                    contentPreview = content.take(200)
                )
            }
        }
    }

    // ==========================================
    // 1. PDF GENERATOR (Native Android PdfDocument)
    // ==========================================
    private fun createPdfDocument(
        context: Context,
        baseName: String,
        topic: String,
        content: String,
        subject: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40
        val contentWidth = pageWidth - (margin * 2)

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#111827")
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val metaPaint = TextPaint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 10f
            isAntiAlias = true
        }

        val headingPaint = TextPaint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#374151")
            textSize = 11f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1.5f
        }

        // Split content into paragraphs
        val lines = content.lines()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = margin + 10

        // Header Banner on Page 1
        canvas.drawText("StudyBuddy Academic Suite • $subject", margin.toFloat(), yPos.toFloat(), metaPaint)
        yPos += 16
        val cleanTitle = if (topic.length > 50) topic.take(47) + "..." else topic
        canvas.drawText(cleanTitle, margin.toFloat(), yPos.toFloat(), titlePaint)
        yPos += 14
        canvas.drawText("Generated on ${SimpleDateFormat("MMMM dd, yyyy • hh:mm a", Locale.US).format(Date())}", margin.toFloat(), yPos.toFloat(), metaPaint)
        yPos += 12
        canvas.drawLine(margin.toFloat(), yPos.toFloat(), (pageWidth - margin).toFloat(), yPos.toFloat(), linePaint)
        yPos += 20

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                yPos += 8
                continue
            }

            val isHeading = trimmed.startsWith("#") || (trimmed.endsWith(":") && trimmed.length < 40)
            val cleanText = trimmed.replace(Regex("^#+\\s*"), "")

            val currentPaint = if (isHeading) headingPaint else bodyPaint
            val layout = StaticLayout.Builder.obtain(
                cleanText,
                0,
                cleanText.length,
                currentPaint,
                contentWidth
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1.15f)
                .setIncludePad(true)
                .build()

            // Check if page fits
            if (yPos + layout.height > pageHeight - margin - 20) {
                // Draw footer page number
                canvas.drawText("Page $pageNumber", (pageWidth / 2f) - 15f, (pageHeight - margin + 15).toFloat(), metaPaint)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = margin

                // Running header on continuation pages
                canvas.drawText("StudyBuddy • $cleanTitle", margin.toFloat(), yPos.toFloat(), metaPaint)
                yPos += 8
                canvas.drawLine(margin.toFloat(), yPos.toFloat(), (pageWidth - margin).toFloat(), yPos.toFloat(), linePaint)
                yPos += 20
            }

            canvas.save()
            canvas.translate(margin.toFloat(), yPos.toFloat())
            layout.draw(canvas)
            canvas.restore()

            yPos += layout.height + (if (isHeading) 6 else 4)
        }

        // Draw last page footer
        canvas.drawText("Page $pageNumber", (pageWidth / 2f) - 15f, (pageHeight - margin + 15).toFloat(), metaPaint)
        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "$baseName.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return outputFile
    }

    // ==========================================
    // 2. MICROSOFT WORD (.docx) GENERATOR
    // ==========================================
    private fun createDocxDocument(
        context: Context,
        baseName: String,
        topic: String,
        content: String,
        subject: String
    ): File {
        val outputFile = File(context.cacheDir, "$baseName.docx")
        val escapedTitle = escapeXml(topic)
        val escapedSubject = escapeXml(subject)

        // Build document.xml paragraphs
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
        sb.append("<w:body>")

        // Document Title
        sb.append("""<w:p><w:pPr><w:jc w:val="center"/></w:pPr>""")
        sb.append("""<w:r><w:rPr><w:b/><w:sz w:val="40"/><w:color w:val="1E293B"/></w:rPr>""")
        sb.append("<w:t>$escapedTitle</w:t></w:r></w:p>")

        // Subtitle / Metadata
        sb.append("""<w:p><w:pPr><w:jc w:val="center"/></w:pPr>""")
        sb.append("""<w:r><w:rPr><w:i/><w:sz w:val="20"/><w:color w:val="64748B"/></w:rPr>""")
        sb.append("<w:t>StudyBuddy Academic Suite • $escapedSubject • ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}</w:t></w:r></w:p>")
        sb.append("<w:p/>")

        // Body content paragraphs
        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                sb.append("<w:p/>")
            } else {
                val isHeading = trimmed.startsWith("#")
                val clean = escapeXml(trimmed.replace(Regex("^#+\\s*"), ""))
                sb.append("<w:p>")
                if (isHeading) {
                    sb.append("""<w:pPr><w:spacing w:before="240" w:after="120"/></w:pPr>""")
                    sb.append("""<w:r><w:rPr><w:b/><w:sz w:val="28"/><w:color w:val="0F172A"/></w:rPr>""")
                } else if (trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.startsWith("*")) {
                    sb.append("""<w:pPr><w:ind w:left="400"/></w:pPr>""")
                    sb.append("""<w:r><w:rPr><w:sz w:val="22"/><w:color w:val="334155"/></w:rPr>""")
                } else {
                    sb.append("""<w:pPr><w:spacing w:after="100"/></w:pPr>""")
                    sb.append("""<w:r><w:rPr><w:sz w:val="22"/><w:color w:val="334155"/></w:rPr>""")
                }
                sb.append("<w:t>$clean</w:t></w:r></w:p>")
            }
        }

        sb.append("</w:body></w:document>")

        // Package into standard OpenXML ZIP archive
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            zos.write(sb.toString().toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }

        return outputFile
    }

    // ==========================================
    // 3. EXCEL SPREADSHEET (.xlsx) GENERATOR
    // ==========================================
    private fun createXlsxSpreadsheet(
        context: Context,
        baseName: String,
        topic: String,
        content: String
    ): File {
        val outputFile = File(context.cacheDir, "$baseName.xlsx")
        val rows = parseTabularData(topic, content)

        // Build sheet1.xml
        val sheetSb = StringBuilder()
        sheetSb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sheetSb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sheetSb.append("<sheetData>")

        rows.forEachIndexed { rIdx, rowCells ->
            val rowNum = rIdx + 1
            sheetSb.append("""<row r="$rowNum">""")
            rowCells.forEachIndexed { cIdx, cellValue ->
                val colLetter = ('A' + (cIdx % 26)).toString()
                val cellRef = "$colLetter$rowNum"
                sheetSb.append("""<c r="$cellRef" t="inlineStr"><is><t>${escapeXml(cellValue)}</t></is></c>""")
            }
            sheetSb.append("</row>")
        }

        sheetSb.append("</sheetData></worksheet>")

        // Package into valid OpenXML XLSX archive
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // xl/_rels/workbook.xml.rels
            zos.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // xl/workbook.xml
            zos.putNextEntry(ZipEntry("xl/workbook.xml"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets><sheet name="StudyData" sheetId="1" r:id="rId1"/></sheets>
</workbook>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // xl/worksheets/sheet1.xml
            zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zos.write(sheetSb.toString().toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }

        return outputFile
    }

    // ==========================================
    // 4. POWERPOINT PRESENTATION (.pptx) GENERATOR
    // ==========================================
    private fun createPptxPresentation(
        context: Context,
        baseName: String,
        topic: String,
        content: String
    ): File {
        val outputFile = File(context.cacheDir, "$baseName.pptx")
        val paragraphs = content.lines().filter { it.isNotBlank() }

        // Break into slide points
        val titleSlideText = escapeXml(topic)
        val bulletPoints = paragraphs.take(8).map { escapeXml(it.replace(Regex("^#+\\s*"), "")) }

        val slide1Xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
  <p:cSld>
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr/>
      <p:sp>
        <p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr><p:nvPr/></p:nvSpPr>
        <p:spPr><a:xfrm><a:off x="1524000" y="2743200"/><a:ext cx="9144000" cy="1828800"/></a:xfrm></p:spPr>
        <p:txBody>
          <a:bodyPr/>
          <a:p><a:r><a:rPr lang="en-US" sz="4400" b="1"/><a:t>$titleSlideText</a:t></a:r></a:p>
          <a:p><a:r><a:rPr lang="en-US" sz="2000" i="1"/><a:t>StudyBuddy Presentation • AI Learning Companion</a:t></a:r></a:p>
        </p:txBody>
      </p:sp>
    </p:spTree>
  </p:cSld>
</p:sld>"""

        val slide2Xml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">""")
            append("<p:cSld><p:spTree>")
            append("""<p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/>""")
            append("<p:sp>")
            append("""<p:nvSpPr><p:cNvPr id="2" name="SlideTitle"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>""")
            append("""<p:spPr><a:xfrm><a:off x="914400" y="548640"/><a:ext cx="10287000" cy="914400"/></a:xfrm></p:spPr>""")
            append("<p:txBody><a:bodyPr/><a:p><a:r><a:rPr lang="en-US" sz="3200" b="1"/><a:t>Key Concepts &amp; Breakdown</a:t></a:r></a:p></p:txBody>")
            append("</p:sp>")
            append("<p:sp>")
            append("""<p:nvSpPr><p:cNvPr id="3" name="Content"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>""")
            append("""<p:spPr><a:xfrm><a:off x="914400" y="1600200"/><a:ext cx="10287000" cy="4572000"/></a:xfrm></p:spPr>""")
            append("<p:txBody><a:bodyPr/>")
            bulletPoints.forEach { pt ->
                append("<a:p><a:pPr lvl=\"0\"/><a:r><a:rPr lang=\"en-US\" sz=\"2000\"/><a:t>• $pt</a:t></a:r></a:p>")
            }
            append("</p:txBody></p:sp>")
            append("</p:spTree></p:cSld></p:sld>")
        }

        // Package into valid OpenXML PPTX
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/slides/slide1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
  <Override PartName="/ppt/slides/slide2.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
</Types>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
</Relationships>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // ppt/_rels/presentation.xml.rels
            zos.putNextEntry(ZipEntry("ppt/_rels/presentation.xml.rels"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide2.xml"/>
</Relationships>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // ppt/presentation.xml
            zos.putNextEntry(ZipEntry("ppt/presentation.xml"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <p:sldIdLst>
    <p:sldId id="256" r:id="rId1"/>
    <p:sldId id="257" r:id="rId2"/>
  </p:sldIdLst>
  <p:sldSz cx="12192000" cy="6858000"/>
</p:presentation>""".toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // ppt/slides/slide1.xml
            zos.putNextEntry(ZipEntry("ppt/slides/slide1.xml"))
            zos.write(slide1Xml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // ppt/slides/slide2.xml
            zos.putNextEntry(ZipEntry("ppt/slides/slide2.xml"))
            zos.write(slide2Xml.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }

        return outputFile
    }

    // ==========================================
    // 5. HTML5 DOCUMENT GENERATOR
    // ==========================================
    private fun createHtmlDocument(
        context: Context,
        baseName: String,
        topic: String,
        content: String
    ): File {
        val outputFile = File(context.cacheDir, "$baseName.html")
        val escapedTitle = escapeXml(topic)
        val formattedBody = content.lines().joinToString("\n") { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("###") -> "<h3>${escapeXml(trimmed.removePrefix("###").trim())}</h3>"
                trimmed.startsWith("##") -> "<h2>${escapeXml(trimmed.removePrefix("##").trim())}</h2>"
                trimmed.startsWith("#") -> "<h1>${escapeXml(trimmed.removePrefix("#").trim())}</h1>"
                trimmed.startsWith("-") || trimmed.startsWith("•") -> "<li>${escapeXml(trimmed.removePrefix("-").removePrefix("•").trim())}</li>"
                trimmed.isNotBlank() -> "<p>${escapeXml(trimmed)}</p>"
                else -> ""
            }
        }

        val htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$escapedTitle - StudyBuddy</title>
  <style>
    :root {
      --bg: #0A0A0A;
      --card-bg: #141414;
      --border: #262626;
      --text-main: #EDEDED;
      --text-sub: #A1A1A1;
      --accent: #3B82F6;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      background-color: var(--bg);
      color: var(--text-main);
      line-height: 1.6;
      margin: 0;
      padding: 32px 16px;
      display: flex;
      justify-content: center;
    }
    .container {
      max-width: 760px;
      width: 100%;
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 32px;
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
    }
    header {
      border-bottom: 1px solid var(--border);
      padding-bottom: 16px;
      margin-bottom: 24px;
    }
    .badge {
      display: inline-block;
      padding: 4px 10px;
      background: #1E293B;
      color: #93C5FD;
      font-size: 12px;
      font-weight: 600;
      border-radius: 999px;
      margin-bottom: 12px;
    }
    h1 { margin: 0 0 8px 0; font-size: 26px; }
    h2 { font-size: 20px; border-bottom: 1px solid var(--border); padding-bottom: 6px; margin-top: 24px; }
    h3 { font-size: 16px; margin-top: 16px; color: #60A5FA; }
    p { margin: 12px 0; color: #D4D4D8; }
    li { margin: 6px 0; color: #D4D4D8; }
    footer {
      margin-top: 32px;
      border-top: 1px solid var(--border);
      padding-top: 16px;
      font-size: 12px;
      color: var(--text-sub);
      text-align: center;
    }
  </style>
</head>
<body>
  <div class="container">
    <header>
      <span class="badge">StudyBuddy Academic Export</span>
      <h1>$escapedTitle</h1>
      <p style="color: var(--text-sub); font-size: 13px; margin: 0;">Exported on ${SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())}</p>
    </header>
    <main>
      $formattedBody
    </main>
    <footer>
      Generated by StudyBuddy AI Companion • Empowering Academic Excellence
    </footer>
  </div>
</body>
</html>"""

        outputFile.writeText(htmlContent, StandardCharsets.UTF_8)
        return outputFile
    }

    // ==========================================
    // 6. CSV GENERATOR
    // ==========================================
    private fun createCsvFile(
        context: Context,
        baseName: String,
        topic: String,
        content: String
    ): File {
        val outputFile = File(context.cacheDir, "$baseName.csv")
        val rows = parseTabularData(topic, content)

        val csvString = rows.joinToString("\n") { rowCells ->
            rowCells.joinToString(",") { cell ->
                val escaped = cell.replace("\"", "\"\"")
                if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
                    "\"$escaped\""
                } else {
                    escaped
                }
            }
        }

        outputFile.writeText(csvString, StandardCharsets.UTF_8)
        return outputFile
    }

    // ==========================================
    // 7. PROJECT ZIP ARCHIVE GENERATOR
    // ==========================================
    private fun createProjectZip(
        context: Context,
        baseName: String,
        topic: String,
        content: String
    ): File {
        val outputFile = File(context.cacheDir, "$baseName.zip")
        val cleanTopic = topic.replace(" ", "_").lowercase(Locale.US)

        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            // README.md
            zos.putNextEntry(ZipEntry("README.md"))
            val readme = """# $topic

Generated by StudyBuddy AI Learning Companion.
Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}

## Summary
$content
"""
            zos.write(readme.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // main index.html
            zos.putNextEntry(ZipEntry("src/index.html"))
            val html = """<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>$topic</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <h1>$topic</h1>
  <div id="content"></div>
  <script src="app.js"></script>
</body>
</html>"""
            zos.write(html.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // style.css
            zos.putNextEntry(ZipEntry("src/style.css"))
            val css = """body {
  font-family: sans-serif;
  background: #111;
  color: #eee;
  padding: 2rem;
}
h1 { color: #3b82f6; }"""
            zos.write(css.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // app.js
            zos.putNextEntry(ZipEntry("src/app.js"))
            val js = """// StudyBuddy Generated Module for: $topic
console.log("StudyBuddy project initialized: ${escapeXml(topic)}");
document.addEventListener("DOMContentLoaded", () => {
  const container = document.getElementById("content");
  if (container) {
    container.innerText = "Loaded project files for $topic";
  }
});"""
            zos.write(js.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // content.txt
            zos.putNextEntry(ZipEntry("docs/notes.txt"))
            zos.write(content.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }

        return outputFile
    }

    // ==========================================
    // 8. PLAIN TEXT / CODE FILE GENERATOR
    // ==========================================
    fun createTextFile(
        context: Context,
        fileName: String,
        content: String,
        mimeType: String
    ): File {
        val file = File(context.cacheDir, fileName)
        file.writeText(content, StandardCharsets.UTF_8)
        return file
    }

    // ==========================================
    // 9. DOWNLOAD & SHARE SYSTEM HOOKS
    // ==========================================

    /**
     * Downloads/Saves the generated file directly to the device's public Downloads directory.
     */
    fun saveToDownloads(context: Context, fileInfo: GeneratedFileInfo): Uri? {
        val sourceFile = fileInfo.localFilePath?.let { File(it) } ?: return null
        if (!sourceFile.exists()) return null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileInfo.fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, fileInfo.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StudyBuddy")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        sourceFile.inputStream().use { inputStream ->
                            inputStream.copyTo(os)
                        }
                    }
                    Toast.makeText(context, "Saved to Downloads: ${fileInfo.fileName}", Toast.LENGTH_LONG).show()
                    return uri
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val studyBuddyDir = File(downloadsDir, "StudyBuddy").apply { mkdirs() }
                val destFile = File(studyBuddyDir, fileInfo.fileName)
                sourceFile.copyTo(destFile, overwrite = true)
                Toast.makeText(context, "Saved to Downloads: ${fileInfo.fileName}", Toast.LENGTH_LONG).show()
                return Uri.fromFile(destFile)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    /**
     * Shares the generated file via Android's native share intent sheet.
     */
    fun shareFile(context: Context, fileInfo: GeneratedFileInfo) {
        val sourceFile = fileInfo.localFilePath?.let { File(it) } ?: return
        if (!sourceFile.exists()) return

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sourceFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = fileInfo.mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, fileInfo.fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share ${fileInfo.fileName}"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the generated file directly in an external viewer (PDF reader, Word, Excel, etc.).
     */
    fun openFile(context: Context, fileInfo: GeneratedFileInfo) {
        val sourceFile = fileInfo.localFilePath?.let { File(it) } ?: return
        if (!sourceFile.exists()) return

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sourceFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, fileInfo.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(viewIntent, "Open with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open ${fileInfo.extension.uppercase()} files", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper: Escapes special XML characters
    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    // Helper: Parses markdown tables or content lines into 2D tabular rows
    private fun parseTabularData(topic: String, content: String): List<List<String>> {
        val tableLines = content.lines().filter { it.contains("|") && it.trim().startsWith("|") }
        if (tableLines.size >= 2) {
            val parsed = mutableListOf<List<String>>()
            tableLines.forEach { line ->
                // Skip separator rows like |---|---|
                if (!line.contains("---")) {
                    val cells = line.split("|")
                        .map { it.trim() }
                        .filterIndexed { index, _ -> index > 0 && index < line.split("|").lastIndex }
                    if (cells.isNotEmpty()) parsed.add(cells)
                }
            }
            if (parsed.isNotEmpty()) return parsed
        }

        // If no markdown table found, construct standard structured educational spreadsheet
        val defaultRows = mutableListOf<List<String>>()
        defaultRows.add(listOf("No.", "Concept / Item", "Key Description", "Category"))

        val contentLines = content.lines().filter { it.isNotBlank() }.take(15)
        contentLines.forEachIndexed { index, line ->
            val clean = line.replace(Regex("^#+\\s*"), "").replace(Regex("^[-*•]\\s*"), "").trim()
            val parts = clean.split(":", limit = 2)
            if (parts.size == 2) {
                defaultRows.add(listOf((index + 1).toString(), parts[0].trim(), parts[1].trim(), topic))
            } else {
                defaultRows.add(listOf((index + 1).toString(), clean.take(35), clean, topic))
            }
        }
        return defaultRows
    }
}
