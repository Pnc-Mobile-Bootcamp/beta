package com.pnc.jetpackcomposedemos.core.observability


import android.util.Log
import timber.log.Timber

/**
 * The Timber tree that feeds the CrashReporter. Warnings and errors become breadcrumbs; an error that carries an
 * exception becomes a report. Debug and verbose messages never leave the device's log.
 */
class CrashReportingTree(
    private val reporter: CrashReporter
) : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        reporter.log("${tag ?: "app"}: $message")
        if (t != null && priority >= Log.ERROR) {
            reporter.recordException(t, message)
        }
    }
}


