package com.example.mastermartini.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/*
 * Intents for the Contacto screen. Each returns false when no app on the device can
 * handle it, so the screen can explain instead of crashing. None of them sends or
 * calls by itself: the user confirms in the email, dialer, WhatsApp or maps app.
 */

/** Email app with recipient, subject and body filled in (ACTION_SENDTO keeps it to email apps). */
fun Context.openEmailDraft(to: String, subject: String, body: String): Boolean {
    val uri = Uri.parse("mailto:$to?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
    return launch(
        Intent(Intent.ACTION_SENDTO, uri)
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, body)
    )
}

/** Dialer with the number typed in; needs no CALL_PHONE permission. */
fun Context.openDialer(phone: String): Boolean = launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))

/** WhatsApp chat (or the wa.me page in a browser if WhatsApp is not installed). */
fun Context.openWhatsApp(number: String, text: String): Boolean =
    launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number?text=${Uri.encode(text)}")))

/** Google Maps search for the address (maps app if installed, otherwise the browser). */
fun Context.openMap(address: String): Boolean =
    launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")))

private fun Context.launch(intent: Intent): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: ActivityNotFoundException) {
    false
}
