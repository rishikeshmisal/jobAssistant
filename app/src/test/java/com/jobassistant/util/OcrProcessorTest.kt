package com.jobassistant.util

import android.graphics.Bitmap
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [OcrProcessor] using MockK to substitute the ML Kit static APIs.
 *
 * [TextRecognition.getClient] and [InputImage.fromBitmap] are both static-factory calls;
 * we mock them with [mockkStatic] so the tests run in a pure JVM without GMS on device.
 */
@Suppress("DEPRECATION") // matching OcrProcessor's own @Suppress for getClient()
class OcrProcessorTest {

    private lateinit var ocrProcessor: OcrProcessor

    @Before
    fun setUp() {
        mockkStatic(TextRecognition::class)
        mockkStatic(InputImage::class)
        ocrProcessor = OcrProcessor()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun extractText_returnsRecognizedTextString() = runTest {
        val bitmap = mockk<Bitmap>()
        val fakeImage = mockk<InputImage>()
        val fakeRecognizer = mockk<TextRecognizer>()
        val fakeTask = mockk<Task<Text>>()
        val fakeText = mockk<Text>()

        every { InputImage.fromBitmap(bitmap, 0) } returns fakeImage
        every { TextRecognition.getClient(any()) } returns fakeRecognizer
        every { fakeRecognizer.process(fakeImage) } returns fakeTask
        every { fakeText.text } returns "Software Engineer at ACME Corp"
        every { fakeTask.addOnSuccessListener(any<OnSuccessListener<Text>>()) } answers {
            @Suppress("UNCHECKED_CAST")
            firstArg<OnSuccessListener<Text>>().onSuccess(fakeText)
            fakeTask
        }
        every { fakeTask.addOnFailureListener(any<OnFailureListener>()) } returns fakeTask

        val result = ocrProcessor.extractText(bitmap)

        assertEquals("Software Engineer at ACME Corp", result)
    }

    @Test
    fun extractText_blankBitmap_returnsEmptyString() = runTest {
        val bitmap = mockk<Bitmap>()
        val fakeImage = mockk<InputImage>()
        val fakeRecognizer = mockk<TextRecognizer>()
        val fakeTask = mockk<Task<Text>>()
        val fakeText = mockk<Text>()

        every { InputImage.fromBitmap(bitmap, 0) } returns fakeImage
        every { TextRecognition.getClient(any()) } returns fakeRecognizer
        every { fakeRecognizer.process(fakeImage) } returns fakeTask
        every { fakeText.text } returns ""
        every { fakeTask.addOnSuccessListener(any<OnSuccessListener<Text>>()) } answers {
            @Suppress("UNCHECKED_CAST")
            firstArg<OnSuccessListener<Text>>().onSuccess(fakeText)
            fakeTask
        }
        every { fakeTask.addOnFailureListener(any<OnFailureListener>()) } returns fakeTask

        val result = ocrProcessor.extractText(bitmap)

        assertEquals("", result)
    }
}
