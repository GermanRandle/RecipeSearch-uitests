package com.erya.recipesearch.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.erya.recipesearch.R
import com.erya.recipesearch.databinding.ActivityMainBinding
import com.erya.recipesearch.presentation.fragments.ButtonFragment
import com.erya.recipesearch.presentation.fragments.OnboardingDialogFragment
import com.project.giniatovia.core.shared.SharedPreferencesKeys

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container, ButtonFragment())
            }
        }

        val prefs = getSharedPreferences(SharedPreferencesKeys.PREFS_FILE, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(SharedPreferencesKeys.FIRST_LAUNCH, true)

        /*if (isFirstLaunch) {
            val prefEditor = prefs.edit()
            prefEditor.putBoolean(SharedPreferencesKeys.FIRST_LAUNCH, false)
            prefEditor.apply()

            OnboardingDialogFragment().show(supportFragmentManager, "onboarding")
        }*/
    }

}