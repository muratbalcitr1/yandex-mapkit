package com.yandex.mapkitdemo

import android.os.Bundle
import android.preference.PreferenceActivity

class MainActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.main)
    }
}
