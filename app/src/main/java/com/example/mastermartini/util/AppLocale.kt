package com.example.mastermartini.util

import android.content.res.Configuration
import java.util.Locale

/**
 * The app is Spanish-only, but Compose components such as the DatePicker format months,
 * weekdays and their screen-reader labels with the device locale. Forcing es-CL keeps the
 * whole UI in Spanish even on a device set to English.
 */
object AppLocale {
    val locale: Locale = Locale.forLanguageTag("es-CL")

    /** Only the locale is set; every other field stays "undefined", so it is not overridden. */
    fun overrideConfiguration(): Configuration = Configuration().apply {
        fontScale = 0f // a new Configuration defaults to 1, which would ignore the user's font size
        setLocale(AppLocale.locale) // qualified: inside apply, `locale` is Configuration's own field
    }
}
