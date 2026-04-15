package com.example.counterapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.counterapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            count = savedInstanceState.getInt(KEY_COUNT, 0)
            updateCountDisplay()
        }

        binding.btnIncrement.setOnClickListener {
            count++
            updateCountDisplay()
        }

        binding.btnDecrement.setOnClickListener {
            if (count > 0) {
                count--
                updateCountDisplay()
            } else {
                Snackbar.make(binding.root, R.string.cannot_go_below_zero, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnReset.setOnClickListener {
            count = 0
            updateCountDisplay()
            Snackbar.make(binding.root, R.string.counter_reset, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_COUNT, count)
    }

    private fun updateCountDisplay() {
        binding.tvCount.text = count.toString()
    }

    companion object {
        private const val KEY_COUNT = "count"
    }
}
