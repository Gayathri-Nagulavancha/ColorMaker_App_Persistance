package com.example.myapplication.finalcolormaker

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import kotlin.math.roundToInt
import android.content.SharedPreferences
import android.content.Context
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import androidx.lifecycle.asLiveData

class MainActivity : AppCompatActivity() {

    private val viewModel: ColorViewModel by viewModels()
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "color_prefs")
    private lateinit var tvColor: TextView
    private var textval = false
    private val colorValue = IntArray(3)
    private val seekbarProgress = IntArray(3)
    private val lastColorValue = IntArray(3)
    private val prevColorValue = IntArray(3)

    

    companion object {
        private const val KEY_COLOR_VALUES = "color_values"
        private const val KEY_TEXT_VALUE = "text_value"
        private const val KEY_SEEK_BAR_PROGRESS = "seek_bar_progress"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvColor = findViewById(R.id.tv_ColorBox)

        val switches = listOf(
            findViewById<Switch>(R.id.switch_1),
            findViewById<Switch>(R.id.switch_2),
            findViewById<Switch>(R.id.switch_3)
        )

        val textViews = arrayOf(
            findViewById<EditText>(R.id.tv_0),
            findViewById<EditText>(R.id.tv_1),
            findViewById<EditText>(R.id.tv_2)
        )

        viewModel.colorValuesFlow.asLiveData().observe(this) { colorValues ->
            colorValue[0] = colorValues.red
            colorValue[1] = colorValues.green
            colorValue[2] = colorValues.blue

            for (index in 0..2) {
                val seekBar = findViewById<SeekBar>(resources.getIdentifier("sb_$index", "id", packageName))
                seekBar.progress = colorValue[index]

                // Update EditTexts
                val editText = textViews[index]
                editText.setText((colorValue[index].toFloat() / 255.toFloat()).toString())
            }
            updateColor()
        }
        val resetButton = findViewById<Button>(R.id.button)
        resetButton.setOnClickListener {
            switches.forEach { it.isChecked = false }
            viewModel.resetColorValues()
            prevColorValue.fill(0) // Add this line to reset the previous color values
            updateColor()
            textViews.forEachIndexed { index, textView ->
                textView.setText("0")
                val seekBar =
                    findViewById<SeekBar>(resources.getIdentifier("sb_$index", "id", packageName))
                seekBar.progress = 0
                seekBar.isEnabled = false
            }
        }

        switches.forEachIndexed { index, switch ->
            val seekBar =
                findViewById<SeekBar>(resources.getIdentifier("sb_$index", "id", packageName))
            val editText = textViews[index]

            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Enable SeekBar and EditText before updating their values
                    seekBar.isEnabled = true
                    editText.isEnabled = true
//                    editText.isFocusable = true
//                    editText.isFocusableInTouchMode = true
                    viewModel.updateValue(index, colorValue[index])
                } else {
                    // Reset SeekBar and EditText values to 0
//                    seekBar.progress = 0
//                    editText.setText("0")

                    // Disable SeekBar and EditText
                    seekBar.isEnabled = false
                    editText.isEnabled = false
//                    editText.isFocusable = false
//                    editText.isFocusableInTouchMode = false
                }

                viewModel.switchStates = viewModel.switchStates.apply { this[index] = isChecked }
                updateColor()
                Log.d(
                    "MainActivity",
                    "Switch state changed for index $index, isChecked = $isChecked, colorValue[$index] = ${colorValue[index]}"
                )
            }
        }

        viewModel.switchStatesFlow.asLiveData().observe(this) { switchStates ->
            switches.forEachIndexed { index, switch ->
                val isChecked = switchStates[index]
                switch.isChecked = isChecked


            }
        }
        textViews.forEachIndexed { index, textView ->
            textView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        val v = s.toString()
                        colorValue[index] = if (v.isEmpty()) 0 else (v.toFloat() * 255).toInt()

                        updateColor()
                        val seekBar = findViewById<SeekBar>(resources.getIdentifier("sb_$index", "id", packageName))
                        seekBar.progress = colorValue[index] // Set the SeekBar progress when EditText value is changed
                    } catch (e: Exception) {
                        // do nothing
                    }
                }


                override fun afterTextChanged(s: Editable?) {}
            })

            val seekBar = findViewById<SeekBar>(resources.getIdentifier("sb_$index", "id", packageName))
            seekBar.isEnabled = switches[index].isChecked
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        colorValue[index] = progress
                        textViews[index].setText((progress.toFloat() / 255.toFloat()).toString())
                        updateColor()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            when (index) {
                0 -> {
                    if (savedInstanceState != null) {
                        colorValue[index] = savedInstanceState.getIntArray(KEY_COLOR_VALUES)?.get(index) ?: 0
                        seekbarProgress[index] = savedInstanceState.getIntArray(KEY_SEEK_BAR_PROGRESS)?.get(index) ?: 0
                        textval = savedInstanceState.getBoolean(KEY_TEXT_VALUE, false)
                    }
                    textView.setText((colorValue[index].toFloat() / 255.toFloat()).toString())
                    seekBar.progress = seekbarProgress[index]
                }
                1 -> {
                    if (savedInstanceState != null) {
                        colorValue[index] = savedInstanceState.getIntArray(KEY_COLOR_VALUES)?.get(index) ?: 0
                        seekbarProgress[index] = savedInstanceState.getIntArray(KEY_SEEK_BAR_PROGRESS)?.get(index) ?: 0
                    }
                    textView.setText((colorValue[index].toFloat() / 255.toFloat()).toString())
                    seekBar.progress = seekbarProgress[index]
                }
                2 -> {
                    if (savedInstanceState != null) {
                        colorValue[index] = savedInstanceState.getIntArray(KEY_COLOR_VALUES)?.get(index) ?: 0
                        seekbarProgress[index] = savedInstanceState.getIntArray(KEY_SEEK_BAR_PROGRESS)?.get(index) ?: 0
                    }
                    textView.setText((colorValue[index].toFloat() / 255.toFloat()).toString())
                    seekBar.progress = seekbarProgress[index]
                }
            }
        }

        updateColor()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(KEY_COLOR_VALUES, colorValue)
        outState.putIntArray(KEY_SEEK_BAR_PROGRESS, seekbarProgress)
        outState.putBoolean(KEY_TEXT_VALUE, textval)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getIntArray(KEY_COLOR_VALUES)?.copyInto(colorValue)
        savedInstanceState.getIntArray(KEY_SEEK_BAR_PROGRESS)?.copyInto(seekbarProgress)
        textval = savedInstanceState.getBoolean(KEY_TEXT_VALUE, false)
        updateColor()
    }

    private fun updateColor() {
        viewModel.redValue = colorValue[0]
        viewModel.greenValue = colorValue[1]
        viewModel.blueValue = colorValue[2]

        tvColor.setBackgroundColor(Color.argb(255, viewModel.redValue, viewModel.greenValue, viewModel.blueValue))
        textval = true
    }
}