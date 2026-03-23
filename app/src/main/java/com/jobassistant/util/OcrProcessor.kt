package com.jobassistant.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Phase 8 — OcrProcessor
 *
 * Wraps ML Kit Text Recognition in a suspend function so callers use it like a regular
 * coroutine call.  Uses the Play-Services-backed (unbundled) recognizer configured via
 * [com.google.android.gms:play-services-mlkit-text-recognition], which is initialised by
 * calling [TextRecognition.getClient] with no arguments (the Play Services bridge injects the
 * recognizer options automatically).
 *
 * On success  → returns the raw recognized [Text.text] string (may be empty for blank images).
 * On failure  → re-throws the exception from ML Kit; callers must handle it.
 */
class OcrProcessor @Inject constructor() {

    suspend fun extractText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return suspendCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result: Text -> cont.resume(result.text) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}
