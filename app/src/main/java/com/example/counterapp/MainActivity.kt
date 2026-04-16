package com.example.counterapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {

    private val DEFAULT_NAMES = arrayOf("まぐろ", "いか", "えび", "ぶり", "たまご", "かずき")

    private val PASTEL_COLORS = intArrayOf(
        Color.parseColor("#E3F2FD"),  // 水色
        Color.parseColor("#FCE4EC"),  // ピンク
        Color.parseColor("#E8F5E9"),  // 緑
        Color.parseColor("#EDE7F6"),  // 紫
        Color.parseColor("#FFF3E0"),  // オレンジ
        Color.parseColor("#FFFDE7"),  // 黄色
        Color.parseColor("#E0F2F1"),  // ティール
        Color.parseColor("#FFEBEE")  // 赤
    )

    private val counts       = mutableListOf<Int>()
    private val names        = mutableListOf<String>()
    private val nameViews    = mutableListOf<TextView>()
    private val countViews   = mutableListOf<TextView>()
    private val colorIndices = mutableListOf<Int>()

    private lateinit var prefs: SharedPreferences
    private lateinit var gridContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private var totalCounters = 0
    private var cardWidth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("sushi_prefs", Context.MODE_PRIVATE)
        totalCounters = prefs.getInt("counter_count", 6)

        for (i in 0 until totalCounters) {
            counts.add(savedInstanceState?.getInt("count_$i", 0) ?: 0)
            val def = if (i < DEFAULT_NAMES.size) DEFAULT_NAMES[i] else "ネタ${i + 1}"
            names.add(prefs.getString("name_$i", def) ?: def)
            colorIndices.add(prefs.getInt("color_$i", 0))
        }

        // カード幅 = (画面幅 - 左右padding(6dp) - 3枚分の左右margin(18dp)) / 3
        cardWidth = (resources.displayMetrics.widthPixels - dp(24)) / 3

        setContentView(buildUI())
    }

    private fun dp(v: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun buildUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F0F0F0"))
        }

        root.addView(TextView(this).apply {
            text = "西川さんお寿司カウンター"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
            maxLines = 1
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT))

        scrollView = ScrollView(this)
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), dp(3), dp(3), dp(10))
        }

        gridContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inner.addView(gridContainer, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        for (i in 0 until totalCounters) appendCard(i)

        inner.addView(buildAddSection(), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
            it.topMargin = dp(8)
        })

        scrollView.addView(inner, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        root.addView(scrollView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        return root
    }

    private fun appendCard(index: Int) {
        val rowIdx = index / 3
        while (gridContainer.childCount <= rowIdx) {
            gridContainer.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val row = gridContainer.getChildAt(rowIdx) as LinearLayout
        // 固定幅（weight不使用）→ 左詰め自動配置
        row.addView(buildCard(index),
            LinearLayout.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                it.setMargins(dp(3), dp(3), dp(3), dp(3))
            })
    }

    private fun buildCard(index: Int): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val nameView = TextView(this).apply {
            text = names[index]
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(3), dp(4), dp(3))
            background = GradientDrawable().apply { setColor(PASTEL_COLORS[colorIndices[index]]) }
            setOnClickListener { showEditDialog(index) }
            setOnLongClickListener { showColorDialog(index); true }
        }
        nameViews.add(nameView)
        card.addView(nameView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val countView = TextView(this).apply {
            text = counts[index].toString()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1976D2"))
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, dp(2))
        }
        countViews.add(countView)
        card.addView(countView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val btnLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val h = dp(36)

        fun btn(label: String, textColor: String, strokeColor: String, action: () -> Unit) =
            Button(this).apply {
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(Color.parseColor(textColor))
                setPadding(0, 0, 0, 0)
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    setStroke(dp(2), Color.parseColor(strokeColor))
                }
                setOnClickListener { action() }
            }

        btnLayout.addView(btn("＋", "#1976D2", "#1976D2") {
            counts[index]++; countView.text = counts[index].toString()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))
        btnLayout.addView(btn("－", "#546E7A", "#546E7A") {
            if (counts[index] > 0) { counts[index]--; countView.text = counts[index].toString() }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))
        btnLayout.addView(btn("×", "#B0BEC5", "#B0BEC5") {
            counts[index] = 0; countView.text = "0"
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))

        card.addView(btnLayout, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        return card
    }

    private fun showColorDialog(index: Int) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        val dialogHolder = arrayOfNulls<AlertDialog>(1)

        // 2行×4列のカラースウォッチ
        for (row in 0..1) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setGravity(Gravity.CENTER)
            }
            for (col in 0..3) {
                val ci = row * 4 + col
                val isSelected = ci == colorIndices[index]
                val swatch = View(this).apply {
                    background = GradientDrawable().apply {
                        setColor(PASTEL_COLORS[ci])
                        setStroke(
                            dp(if (isSelected) 4 else 2),
                            if (isSelected) Color.parseColor("#1976D2") else Color.LTGRAY
                        )
                        setCornerRadius(dp(8).toFloat())
                    }
                    setOnClickListener {
                        colorIndices[index] = ci
                        nameViews[index].background =
                            GradientDrawable().apply { setColor(PASTEL_COLORS[ci]) }
                        prefs.edit().putInt("color_$index", ci).apply()
                        dialogHolder[0]?.dismiss()
                    }
                }
                rowLayout.addView(swatch,
                    LinearLayout.LayoutParams(dp(52), dp(52)).also {
                        it.setMargins(dp(6), dp(6), dp(6), dp(6))
                    })
            }
            container.addView(rowLayout)
        }

        dialogHolder[0] = AlertDialog.Builder(
            this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("色を選択（長押しで変更）")
            .setView(container)
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun buildAddSection(): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }

        val edit = EditText(this).apply {
            hint = "名前を入力して追加"
            inputType = InputType.TYPE_CLASS_TEXT
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        }
        val addBtn = Button(this).apply {
            text = "追加"
            setTextColor(Color.parseColor("#1976D2"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#1976D2"))
            }
        }

        addBtn.setOnClickListener {
            val name = edit.text.toString().trim()
            if (name.isNotEmpty()) {
                val idx = totalCounters
                counts.add(0)
                names.add(name)
                colorIndices.add(0)
                prefs.edit()
                    .putString("name_$idx", name)
                    .putInt("counter_count", totalCounters + 1)
                    .putInt("color_$idx", 0)
                    .apply()
                totalCounters++
                appendCard(idx)
                edit.text.clear()
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }

        layout.addView(edit, LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        layout.addView(addBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        return layout
    }

    private fun showEditDialog(index: Int) {
        val edit = EditText(this).apply {
            setText(names.getOrElse(index) { "" })
            inputType = InputType.TYPE_CLASS_TEXT
            selectAll()
        }
        val wrap = LinearLayout(this).apply { setPadding(60, 20, 60, 20) }
        wrap.addView(edit)

        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("名前を変更")
            .setView(wrap)
            .setPositiveButton("変更") { _, _ ->
                val name = edit.text.toString().trim()
                if (name.isNotEmpty()) {
                    names[index] = name
                    nameViews[index].text = name
                    prefs.edit().putString("name_$index", name).apply()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in 0 until totalCounters) outState.putInt("count_$i", counts[i])
    }
}
