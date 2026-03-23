package com.jobassistant.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlStripperTest {

    @Test
    fun stripHtml_removesSimpleTags() {
        val input = "<p>Hello <b>world</b></p>"
        val result = HtmlStripper.stripHtml(input)
        assertFalse("Should not contain <p>", "<p>" in result)
        assertFalse("Should not contain <b>", "<b>" in result)
        assertTrue("Should contain hello world", "Hello" in result && "world" in result)
    }

    @Test
    fun stripHtml_removesScriptBlock() {
        val input = "<div>Intro</div><script>alert('xss')</script><p>Content</p>"
        val result = HtmlStripper.stripHtml(input)
        assertFalse("Should not contain script tag", "<script>" in result)
        assertFalse("Should not contain script content", "alert" in result)
        assertTrue("Should contain page content", "Intro" in result && "Content" in result)
    }

    @Test
    fun stripHtml_removesStyleBlock() {
        val input = "<style>.body { color: red; font-size: 14px; }</style><p>Visible text</p>"
        val result = HtmlStripper.stripHtml(input)
        assertFalse("Should not contain style tag", "<style>" in result)
        assertFalse("Should not contain CSS", "color: red" in result)
        assertTrue("Should contain visible text", "Visible text" in result)
    }

    @Test
    fun stripHtml_decodesHtmlEntities() {
        val input = "<p>5 &lt; 10 &amp; 3 &gt; 2 &nbsp; hello &quot;world&quot;</p>"
        val result = HtmlStripper.stripHtml(input)
        assertTrue("Should decode &lt; to <", "<" in result)
        assertTrue("Should decode &gt; to >", ">" in result)
        assertTrue("Should decode &amp; to &", "&" in result)
        assertTrue("Should decode &quot; to \"", "\"" in result)
        assertFalse("Should not contain raw entity &lt;", "&lt;" in result)
        assertFalse("Should not contain raw entity &amp;", "&amp;" in result)
    }

    @Test
    fun stripHtml_collapsesWhitespace() {
        val input = "<p>  Too   many    spaces   </p>"
        val result = HtmlStripper.stripHtml(input)
        assertFalse("Should not contain multiple spaces", "  " in result)
        assertTrue("Should contain the text", "Too" in result && "many" in result && "spaces" in result)
    }

    @Test
    fun stripHtml_handlesNestedTags() {
        val input = "<div class=\"wrapper\"><section><article><h1>Title</h1><p>Body text here.</p></article></section></div>"
        val result = HtmlStripper.stripHtml(input)
        assertFalse("Should not contain any HTML tags", "<" in result && ">" in result && "div" in result)
        assertTrue("Should contain title", "Title" in result)
        assertTrue("Should contain body", "Body text here." in result)
    }

    @Test
    fun stripHtml_returnsEmptyStringForEmptyInput() {
        val result = HtmlStripper.stripHtml("")
        assertTrue("Empty input should return empty", result.isEmpty())
    }
}
