package com.example.counterapp

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {

    private val counts = IntArray(6)

    private val countViewIds = intArrayOf(
        R.id.tv_count_0, R.id.tv_count_1, R.id.tv_count_2,
        R.id.tv_count_3, R.id.tv_count_4, R.id.tv_count_5
    )
    private val incIds = intArrayOf(
        R.id.btn_inc_0, R.id.btn_inc_1, R.id.btn_inc_2,
        R.id.btn_inc_3, R.id.btn_inc_4, R.id.btn_inc_5
    )
    private val decIds = intArrayOf(
        R.id.btn_dec_0, R.id.btn_dec_1, R.id.btn_dec_2,
        R.id.btn_dec_3, R.id.btn_dec_4, R.id.btn_dec_5
    )
    private val rstIds = intArrayOf(
        R.id.btn_rst_0, R.id.btn_rst_1, R.id.btn_rst_2,
        R.id.btn_rst_3, R.id.btn_rst_4, R.id.btn_rst_5
    )

    private lateinit var countViews: Array<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            for (i in 0..5) counts[i] = savedInstanceState.getInt("count_$i", 0)
        }

        countViews = Array(6) { i -> findViewById(countViewIds[i]) as TextView }

        for (i in 0..5) {
            countViews[i].text = counts[i].toString()

            (findViewById(incIds[i]) as Button).setOnClickListener {
                counts[i]++
                countViews[i].text = counts[i].toString()
            }
            (findViewById(decIds[i]) as Button).setOnClickListener {
                if (counts[i] > 0) {
                    counts[i]--
                    countViews[i].text = counts[i].toString()
                }
            }
            (findViewById(rstIds[i]) as Button).setOnClickListener {
                counts[i] = 0
                countViews[i].text = "0"
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in 0..5) outState.putInt("count_$i", counts[i])
    }
}
