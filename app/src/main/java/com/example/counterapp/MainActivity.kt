package com.example.counterapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val DEFAULT_NAMES = arrayOf("まぐろ", "いか", "えび", "ぶり", "たまご", "かずき")

    private val counts = IntArray(6)

    // 個別変数で保持（ジェネリック配列を避ける）
    private var tvName0: TextView? = null; private var tvName1: TextView? = null
    private var tvName2: TextView? = null; private var tvName3: TextView? = null
    private var tvName4: TextView? = null; private var tvName5: TextView? = null

    private var tvCount0: TextView? = null; private var tvCount1: TextView? = null
    private var tvCount2: TextView? = null; private var tvCount3: TextView? = null
    private var tvCount4: TextView? = null; private var tvCount5: TextView? = null

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("sushi_prefs", Context.MODE_PRIVATE)

        if (savedInstanceState != null) {
            for (i in 0..5) counts[i] = savedInstanceState.getInt("count_$i", 0)
        }

        // ビューを取得
        tvName0 = findViewById(R.id.tv_name_0) as TextView
        tvName1 = findViewById(R.id.tv_name_1) as TextView
        tvName2 = findViewById(R.id.tv_name_2) as TextView
        tvName3 = findViewById(R.id.tv_name_3) as TextView
        tvName4 = findViewById(R.id.tv_name_4) as TextView
        tvName5 = findViewById(R.id.tv_name_5) as TextView

        tvCount0 = findViewById(R.id.tv_count_0) as TextView
        tvCount1 = findViewById(R.id.tv_count_1) as TextView
        tvCount2 = findViewById(R.id.tv_count_2) as TextView
        tvCount3 = findViewById(R.id.tv_count_3) as TextView
        tvCount4 = findViewById(R.id.tv_count_4) as TextView
        tvCount5 = findViewById(R.id.tv_count_5) as TextView

        val nameViews  = listOf(tvName0,  tvName1,  tvName2,  tvName3,  tvName4,  tvName5)
        val countViews = listOf(tvCount0, tvCount1, tvCount2, tvCount3, tvCount4, tvCount5)

        val incIds = intArrayOf(R.id.btn_inc_0, R.id.btn_inc_1, R.id.btn_inc_2,
                                R.id.btn_inc_3, R.id.btn_inc_4, R.id.btn_inc_5)
        val decIds = intArrayOf(R.id.btn_dec_0, R.id.btn_dec_1, R.id.btn_dec_2,
                                R.id.btn_dec_3, R.id.btn_dec_4, R.id.btn_dec_5)
        val rstIds = intArrayOf(R.id.btn_rst_0, R.id.btn_rst_1, R.id.btn_rst_2,
                                R.id.btn_rst_3, R.id.btn_rst_4, R.id.btn_rst_5)

        for (i in 0..5) {
            val nv = nameViews[i] ?: continue
            val cv = countViews[i] ?: continue

            // 保存済みの名前をセット
            nv.text = prefs.getString("name_$i", DEFAULT_NAMES[i]) ?: DEFAULT_NAMES[i]
            cv.text = counts[i].toString()

            // 名前タップで編集（コードでclickable設定）
            val idx = i
            nv.setOnClickListener { showEditDialog(idx) }

            (findViewById(incIds[i]) as Button).setOnClickListener {
                counts[idx]++
                cv.text = counts[idx].toString()
            }
            (findViewById(decIds[i]) as Button).setOnClickListener {
                if (counts[idx] > 0) { counts[idx]--; cv.text = counts[idx].toString() }
            }
            (findViewById(rstIds[i]) as Button).setOnClickListener {
                counts[idx] = 0; cv.text = "0"
            }
        }
    }

    private fun nameViewAt(index: Int): TextView? = when (index) {
        0 -> tvName0; 1 -> tvName1; 2 -> tvName2
        3 -> tvName3; 4 -> tvName4; 5 -> tvName5
        else -> null
    }

    private fun showEditDialog(index: Int) {
        val current = nameViewAt(index)?.text?.toString() ?: DEFAULT_NAMES[index]

        val edit = EditText(this)
        edit.setText(current)
        edit.inputType = InputType.TYPE_CLASS_TEXT
        edit.selectAll()

        val wrap = LinearLayout(this)
        wrap.setPadding(60, 20, 60, 20)
        wrap.addView(edit)

        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("名前を変更")
            .setView(wrap)
            .setPositiveButton("変更") { _, _ ->
                val name = edit.text.toString().trim()
                if (name.length > 0) {
                    nameViewAt(index)?.text = name
                    prefs.edit().putString("name_$index", name).apply()
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
