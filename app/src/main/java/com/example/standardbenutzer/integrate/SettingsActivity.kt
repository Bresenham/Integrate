package com.example.standardbenutzer.integrate

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.preference.Preference.OnPreferenceClickListener
import android.util.Log
import android.widget.Toast
import java.util.concurrent.RejectedExecutionException
import android.preference.SwitchPreference
import android.preference.Preference.OnPreferenceChangeListener

/**
 * Created by Standardbenutzer on 31.12.2017.
 */
class SettingsActivity : AppCompatActivity {
    constructor() : super()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                onBackPressed()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    class SettingsFragment : PreferenceFragment {
        private val NUMBER_OF_TASKS = Runtime.getRuntime().availableProcessors()
        constructor() : super()
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            findPreference("benchmark").onPreferenceClickListener = OnPreferenceClickListener {
                startAsyncIntegration(-0.125,0.125)
                true
            }

            findPreference("incrPrecisionPreference").onPreferenceChangeListener = OnPreferenceChangeListener { preference, _ ->
                val sharedPref = activity.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("incrPrecisionPreference", !(preference as SwitchPreference).isChecked)
                editor.apply()
                true
            }
        }

        private fun startAsyncIntegration(a : Double, b : Double){
            val list = mutableListOf<Double>()

            val start = System.currentTimeMillis()

            for(i in 0..NUMBER_OF_TASKS-1){
                val adapt = AsyncAdaptiveIntegration()
                val a1 = a + (((b-a) / NUMBER_OF_TASKS) * i)
                val b1 = a + (((b-a) / NUMBER_OF_TASKS) * (i + 1))
                try {
                    adapt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "1/(0.0000001 + x^2)", a1, b1, 0.000000001, object : OnAdaptiveIntegrationCompleted {
                        override fun onAdaptiveIntegrationCompleted(result: Double?, sumList: MutableList<Double>) {
                            if (sumList.count() == NUMBER_OF_TASKS) {
                                val end = System.currentTimeMillis()
                                Toast.makeText(context,"It took %03d ms to run the Benchmark on your $NUMBER_OF_TASKS cores.".format(end-start),Toast.LENGTH_LONG).show()
                                list.clear()
                            }
                        }
                    }, list)
                } catch (e : RejectedExecutionException){
                    Log.d("ERROR", e.message)
                }
            }
        }
    }
}