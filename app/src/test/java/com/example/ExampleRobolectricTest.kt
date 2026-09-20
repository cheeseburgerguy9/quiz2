package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Caitlin Daily", appName)
  }

  @Test
  fun `verify easter egg friday logic message`() {
    val message = com.example.util.SoundEffectHelper.getFridayEasterEggMessage()
    org.junit.Assert.assertTrue(
      message == "congrats today's the day , enjoy ." || message.contains("days till the next friday") || message == "1 day till the next friday"
    )
  }
}
