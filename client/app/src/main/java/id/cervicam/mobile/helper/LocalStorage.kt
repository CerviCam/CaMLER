package id.cervicam.mobile.helper

import android.content.Context
import android.content.SharedPreferences

class LocalStorage {
    enum class PreferenceKeys(val value: String) {
        ID("ID"),
        USERNAME("USERNAME"),
        PASSWORD("PASSWORD"),
        TOKEN("TOKEN")
    }

    companion object {
        private const val PREFERENCE_ID = "CERVICAM_PREF"

        private fun getPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                PREFERENCE_ID,
                Context.MODE_PRIVATE
            )
        }

        fun set(context: Context, key: String, value: String) {
            val sharedPreferences = getPreferences(context)
            val sharedPreferencesEditor: SharedPreferences.Editor = sharedPreferences.edit()
            sharedPreferencesEditor.putString(key, value)
            sharedPreferencesEditor.apply()
        }

        fun get(context: Context, key: String): String? {
            val sharedPreferences = getPreferences(context)
            return sharedPreferences.getString(key, null)
        }
    }
}