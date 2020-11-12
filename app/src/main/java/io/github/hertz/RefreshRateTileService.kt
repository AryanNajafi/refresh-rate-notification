package io.github.hertz

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class RefreshRateTileService : TileService() {

    override fun onStartListening() {

        val tile = qsTile

        tile.state = if (RefreshRateService.ServiceState.started)
            Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

        tile.updateTile()
    }

    override fun onClick() {
        val serviceStarted = RefreshRateService.ServiceState.started

        val serviceIntent = Intent(this, RefreshRateService::class.java)
        if (serviceStarted) {
            stopService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}