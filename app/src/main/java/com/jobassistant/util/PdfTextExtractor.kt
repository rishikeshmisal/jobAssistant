package com.jobassistant.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Extracts readable text from a PDF document.
 *
 * Strategy:
 * 1. **PDFBox** — strips native text streams directly from the PDF; works for any text-based PDF
 *    in O(pages) time with no quality loss.
 * 2. **OCR fallback** — if PDFBox returns blank text (scanned / image-only PDF), renders each
 *    page via [PdfRenderer] and runs ML Kit text recognition.
 *
 * This replaces the previous render-then-OCR-only approach which lost formatting and failed
 * silently on normal PDFs.
 */
class PdfTextExtractor @Inject constructor() {

    suspend fun extract(uri: Uri, context: Context): String? = withContext(Dispatchers.IO) {
        PDFBoxResourceLoader.init(context)
        val nativeText = tryPdfBoxExtract(uri, context)
        if (!nativeText.isNullOrBlank()) return@withContext nativeText
        tryOcrExtract(uri, context)
    }

    // ── PDFBox native text extraction ─────────────────────────────────────────

    private fun tryPdfBoxExtract(uri: Uri, context: Context): String? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            inputStream.use { stream ->
                PDDocument.load(stream).use { document ->
                    if (document.numberOfPages == 0) return null
                    PDFTextStripper().getText(document).takeIf { it.isNotBlank() }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    // ── OCR fallback (scanned / image-only PDFs) ──────────────────────────────

    private fun tryOcrExtract(uri: Uri, context: Context): String? {
        return try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return null
            parcelFileDescriptor.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val sb = StringBuilder()
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    for (i in 0 until renderer.pageCount) {
                        renderer.openPage(i).use { page ->
                            val bitmap = Bitmap.createBitmap(
                                page.width, page.height, Bitmap.Config.ARGB_8888
                            )
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            val pageText = try {
                                val result = Tasks.await(
                                    recognizer.process(InputImage.fromBitmap(bitmap, 0)),
                                    10, TimeUnit.SECONDS
                                )
                                result.text
                            } catch (e: Exception) {
                                ""
                            }
                            if (pageText.isNotBlank()) sb.append(pageText).append("\n")
                            bitmap.recycle()
                        }
                    }
                    recognizer.close()
                    sb.toString().takeIf { it.isNotBlank() }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
