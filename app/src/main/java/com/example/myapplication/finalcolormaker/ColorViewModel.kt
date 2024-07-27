package com.example.myapplication.finalcolormaker

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.content.Context

private const val COLOR_PREFERENCES_NAME = "color_prefs"

class ColorViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore: DataStore<Preferences> = getApplication<Application>().colorPreferences

    private val redValueKey = intPreferencesKey("red_value")
    private val greenValueKey = intPreferencesKey("green_value")
    private val blueValueKey = intPreferencesKey("blue_value")
    private val switchStatesKey = stringPreferencesKey("switch_states")
    private val seekBarProgressKey = stringPreferencesKey("seek_bar_progress")


    var redValue: Int = 0
        set(value) {
            field = value
            saveColorValues()
        }

    var greenValue: Int = 0
        set(value) {
            field = value
            saveColorValues()
        }

    var blueValue: Int = 0
        set(value) {
            field = value
            saveColorValues()
        }

    var switchStates: BooleanArray = booleanArrayOf(false, false, false)
        set(value) {
            field = value
            saveSwitchStates()
        }

    val colorValuesFlow: Flow<ColorValues> = dataStore.data.map { preferences ->
        ColorValues(
            preferences[redValueKey] ?: 0,
            preferences[greenValueKey] ?: 0,
            preferences[blueValueKey] ?: 0
        )
    }

    val switchStatesFlow: Flow<BooleanArray> = dataStore.data.map { preferences ->
        preferences[switchStatesKey]?.split(",")?.map { it.toBoolean() }?.toBooleanArray() ?: booleanArrayOf(false, false, false)
    }

    val seekBarProgressFlow: Flow<IntArray> = dataStore.data.map { preferences ->
        preferences[seekBarProgressKey]?.split(",")?.map { it.toInt() }?.toIntArray() ?: intArrayOf(0, 0, 0)
    }

    fun updateValue(index: Int, value: Int) {
        when (index) {
            0 -> redValue = value
            1 -> greenValue = value
            2 -> blueValue = value
        }
        saveColorValues()
    }

    fun resetColorValues() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[redValueKey] = 0
                preferences[greenValueKey] = 0
                preferences[blueValueKey] = 0
            }
        }
    }

    private fun saveColorValues() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[redValueKey] = redValue
                preferences[greenValueKey] = greenValue
                preferences[blueValueKey] = blueValue
            }
        }
    }

    private fun saveSwitchStates() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[switchStatesKey] = switchStates.joinToString(",")
            }
        }
    }

    fun saveSeekBarProgress(progress: IntArray) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[seekBarProgressKey] = progress.joinToString(",")
            }
        }
    }
}

data class ColorValues(val red: Int, val green: Int, val blue: Int)

private val Context.colorPreferences: DataStore<Preferences> by preferencesDataStore(name = COLOR_PREFERENCES_NAME)
