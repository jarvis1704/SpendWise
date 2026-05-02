package com.biprangshu.spendwise.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), STARTUP_TIMEOUT_MS)

        tapByDescription("History")
        tapByDescription("Insights")
        tapByDescription("Home")
        tapByDescription("Add Transaction")
        device.pressBack()
    }

    private fun MacrobenchmarkScope.tapByDescription(description: String) {
        val objectToClick = device.wait(
            Until.findObject(By.desc(description)),
            UI_TIMEOUT_MS
        ) ?: return

        objectToClick.click()
        device.waitForIdle()
    }

    private companion object {
        const val PACKAGE_NAME = "com.biprangshu.spendwise"
        const val STARTUP_TIMEOUT_MS = 5_000L
        const val UI_TIMEOUT_MS = 2_000L
    }
}
