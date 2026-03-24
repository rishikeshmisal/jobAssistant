package com.jobassistant.ui.theme

import androidx.compose.ui.graphics.Color
import com.jobassistant.domain.model.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppColorsTest {

    @Test
    fun allThemes_haveNonNullSeedColor() {
        for (theme in AppTheme.values()) {
            assertNotNull("Seed color missing for theme $theme", ThemeSeedColors[theme])
        }
    }

    @Test
    fun seedColors_coverAllFourThemes() {
        assertEquals(AppTheme.values().size, ThemeSeedColors.size)
    }

    @Test
    fun greenTheme_hasExpectedSeedColor() {
        assertEquals(Color(0xFF2E7D32), ThemeSeedColors[AppTheme.GREEN])
    }

    @Test
    fun redTheme_hasExpectedSeedColor() {
        assertEquals(Color(0xFFC62828), ThemeSeedColors[AppTheme.RED])
    }

    @Test
    fun blueTheme_hasExpectedSeedColor() {
        assertEquals(Color(0xFF1565C0), ThemeSeedColors[AppTheme.BLUE])
    }

    @Test
    fun purpleTheme_hasExpectedSeedColor() {
        assertEquals(Color(0xFF6A1B9A), ThemeSeedColors[AppTheme.PURPLE])
    }
}
