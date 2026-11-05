package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiVisionService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name and verify multilingual semantic expansion`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FX AI Unity", appName)

        val sunsetSynonyms = GeminiVisionService.getInstantMultilingualSynonyms("डूबता हुआ सूरज")
        assertTrue(sunsetSynonyms.contains("sunset"))
        assertTrue(sunsetSynonyms.contains("doobta hua suraj"))
    }
}
