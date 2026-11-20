package com.example.tp_android_contacts_v3.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.tp_android_contacts_v3.R

class ParametresFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
