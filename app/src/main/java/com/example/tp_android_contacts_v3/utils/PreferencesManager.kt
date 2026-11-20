package com.example.tp_android_contacts_v3.utils

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

class PreferencesManager(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getFontSize(): Float {
        val tailleStr = prefs.getString("taille_police", "16")
        return tailleStr?.toFloatOrNull() ?: 16f
    }

    fun getTextColor(): Int {
        val colorHex = prefs.getString("couleur_preferee", "#000000")
        return try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.BLACK
        }
    }

    fun isCallFromListEnabled(): Boolean {
        return prefs.getBoolean("appel_depuis_liste", false)
    }
}
