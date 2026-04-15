package com.example.counterapp

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var count = 0
    private lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCount = findViewById(R.id.tv_count) as TextView

        if (savedInstanceState != null) {
            count = savedInstanceState.getInt("count", 0)
            tvCount.text = count.toString()
        }

        (findViewById(R.id.btn_increment) as Button).setOnClickListener {
            count++
            tvCount.text = count.toString()
        }

        (findViewById(R.id.btn_decrement) as Button).setOnClickListener {
            if (count > 0) {
                count--
                tvCount.text = count.toString()
            } else {
                Toast.makeText(this, "0より小さくはできません", Toast.LENGTH_SHORT).show()
            }
        }

        (findViewById(R.id.btn_reset) as Button).setOnClickListener {
            count = 0
            tvCount.text = count.toString()
            Toast.makeText(this, "カウンターをリセットしました", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("count", count)
    }
}
