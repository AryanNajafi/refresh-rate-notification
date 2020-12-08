package io.github.hertz

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var refreshRateSwitch: SwitchMaterial

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshRateSwitch = findViewById(R.id.refresh_rate_switch)

        refreshRateSwitch.setOnCheckedChangeListener(onCheckedChangeListener)

        val displayManager =
            getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        val refreshRateView: TextView = findViewById(R.id.refresh_rate_value)

        displayManager.refreshRateFlow()
            .asLiveData()
            .observe(this, { refreshRate ->
                refreshRateView.text = refreshRate.toString()
            })
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        refreshRateSwitch.isChecked = RefreshRateService.ServiceState.started
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_KEY_REFRESH_RATE) {
            updateSwitchCheckState(sharedPreferences!!
                .getBoolean(PREF_KEY_REFRESH_RATE, false))
        }
    }

    private val onCheckedChangeListener: (CompoundButton, Boolean) -> Unit = { _, checked ->
        val serviceIntent = Intent(this, RefreshRateService::class.java)
        if (checked) {
            startForegroundService(serviceIntent)
        } else {
            stopService(serviceIntent)
        }
    }

    private fun updateSwitchCheckState(checked: Boolean) {
        refreshRateSwitch.setOnCheckedChangeListener(null)
        refreshRateSwitch.isChecked = checked
        refreshRateSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
    }
}