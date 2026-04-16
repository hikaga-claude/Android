package com.example.counterapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val defaultNames = arrayOf("まぐろ", "いか", "えび", "ぶり", "たまご", "かずき")
    private val counts = IntArray(6)

    private val nameViewIds = intArrayOf(
        R.id.tv_name_0, R.id.tv_name_1, R.id.tv_name_2,
        R.id.tv_name_3, R.id.tv_name_4, R.id.tv_name_5
    )
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

    private lateinit var nameViews: Array<TextView>
    private lateinit var countViews: Array<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("sushi_prefs", Context.MODE_PRIVATE)

        if (savedInstanceState != null) {
            for (i in 0..5) counts[i] = savedInstanceState.getInt("count_$i", 0)
        }

        nameViews  = Array(6) { i -> findViewById(nameViewIds[i])  as TextView }
        countViews = Array(6) { i -> findViewById(countViewIds[i]) as TextView }

        for (i in 0..5) {
            // SharedPreferencesから名前を読み込み（なければデフォルト値）
            nameViews[i].text = prefs.getString("name_$i", defaultNames[i]) ?: defaultNames[i]
            countViews[i].text = counts[i].toString()

            // 名前をタップで編集ダイアログ
            nameViews[i].setOnClickListener {
                showEditNameDialog(i, nameViews[i].text.toString(), prefs)
            }

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

    private fun showEditNameDialog(index: Int, currentName: String,
                                   prefs: android.content.SharedPreferences) {
        val editText = EditText(this).apply {
            setText(currentName)
            inputType = InputType.TYPE_CLASS_TEXT
            selectAll()
        }
        val container = LinearLayout(this).apply {
            setPadding(60, 20, 60, 20)
            addView(editText)
        }

        AlertDialog.Builder(this)
            .setTitle("名前を変更")
            .setView(container)
            .setPositiveButton("変更") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    nameViews[index].text = newName
                    prefs.edit().putString("name_$index", newName).apply()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in 0..5) outState.putInt("count_$i", counts[i])
    }
}
