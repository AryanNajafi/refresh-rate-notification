package io.github.hertz

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var refreshRateSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshRateSwitch = findViewById(R.id.refresh_rate_switch)

        refreshRateSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
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
            updateSwitchCheckState(sharedPreferences!!.getBoolean(PREF_KEY_REFRESH_RATE, false))
        }
    }

    private val onCheckedChangeListener: (CompoundButton, Boolean) -> Unit = { _, checked ->
        val serviceIntent = Intent(this, RefreshRateService::class.java)
        if (checked) {
            startService(serviceIntent)
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