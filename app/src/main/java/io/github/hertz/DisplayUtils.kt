package io.github.hertz

import android.hardware.display.DisplayManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
fun DisplayManager.refreshRateFlow() = callbackFlow {
    val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(p0: Int) {
        }

        override fun onDisplayRemoved(p0: Int) {
        }

        override fun onDisplayChanged(displayId: Int) {
            offer(getDisplay(displayId).refreshRate.roundToInt())
        }
    }
    registerDisplayListener(displayListener, null)

    offer(displays[0].refreshRate.roundToInt())

    awaitClose {
        unregisterDisplayListener(displayListener)
    }
}