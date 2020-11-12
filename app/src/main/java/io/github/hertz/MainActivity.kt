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

        refreshRateSwitch.isChecked = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PREF_KEY_REFRESH_RATE, false)

        refreshRateSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_KEY_REFRESH_RATE) {
            refreshRateSwitch.setOnCheckedChangeListener(null)
            refreshRateSwitch.isChecked =
                    sharedPreferences!!.getBoolean(PREF_KEY_REFRESH_RATE, false)
            refreshRateSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
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
}