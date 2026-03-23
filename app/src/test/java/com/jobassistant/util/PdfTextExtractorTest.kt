package com.jobassistant.util

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [PdfTextExtractor].
 *
 * PDFBoxResourceLoader.init(context) calls context.getAssets() to load font files, so we spy on
 * the real Robolectric application context and redirect only contentResolver to a mock that
 * exercises each error branch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class PdfTextExtractorTest {

    private lateinit var extractor: PdfTextExtractor
    private lateinit var context: Application
    private lateinit var mockResolver: ContentResolver

    @Before
    fun setUp() {
        extractor = PdfTextExtractor()
        context = spyk(ApplicationProvider.getApplicationContext())
        mockResolver = mockk()
        every { context.contentResolver } returns mockResolver
        // Default: PDFBox path returns null (no content) — OCR path will also be tested below
        every { mockResolver.openInputStream(any()) } returns null
    }

    @Test
    fun `extract returns null when both paths return null fd and null stream`() = runTest {
        every { mockResolver.openFileDescriptor(any(), any()) } returns null

        val result = extractor.extract(Uri.EMPTY, context)

        assertNull(result)
    }

    @Test
    fun `extract returns null when openInputStream throws and openFileDescriptor throws`() = runTest {
        every { mockResolver.openInputStream(any()) } throws Exception("IO error")
        every { mockResolver.openFileDescriptor(any(), any()) } throws Exception("IO error")

        val result = extractor.extract(Uri.EMPTY, context)

        assertNull(result)
    }

    @Test
    fun `extract returns null for invalid uri when both paths throw`() = runTest {
        every { mockResolver.openInputStream(any()) } throws IllegalArgumentException("Invalid URI")
        every { mockResolver.openFileDescriptor(any(), "r") } throws IllegalArgumentException("Invalid URI")

        val result = extractor.extract(Uri.parse("invalid://resource"), context)

        assertNull(result)
    }

    @Test
    fun `PdfTextExtractor can be instantiated`() {
        assertNotNull(PdfTextExtractor())
    }

    @Test
    fun `extract returns null for corrupted pdf bytes`() = runTest {
        val corruptFile = java.io.File.createTempFile("corrupt_", ".pdf")
        corruptFile.writeBytes("this is not a valid pdf file".toByteArray())
        val fileUri = Uri.fromFile(corruptFile)
        val realContext = ApplicationProvider.getApplicationContext<Application>()

        val result = extractor.extract(fileUri, realContext)

        assertNull("Corrupted PDF should return null", result)
        corruptFile.delete()
    }

    @Test
    fun `extract handles security exception gracefully`() = runTest {
        every { mockResolver.openInputStream(any()) } throws SecurityException("Permission denied")
        every { mockResolver.openFileDescriptor(any(), any()) } throws SecurityException("Permission denied")

        val result = extractor.extract(Uri.EMPTY, context)

        assertNull("SecurityException should return null", result)
    }
}
