package com.example.counterapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
    private val DEFAULT_COLOR_INDICES = intArrayOf(14, 16, 19, 18, 15, 4)

    // ─── カラーパレット定義（各20色）───────────────────────────
    private val PALETTE_PASTEL = intArrayOf(
        Color.parseColor("#FFCDD2"), Color.parseColor("#F8BBD0"), Color.parseColor("#FFADD2"), Color.parseColor("#FFB3C1"),
        Color.parseColor("#E1BEE7"), Color.parseColor("#D1C4E9"), Color.parseColor("#C5CAE9"), Color.parseColor("#D4C5E2"),
        Color.parseColor("#BBDEFB"), Color.parseColor("#B3E5FC"), Color.parseColor("#B2EBF2"), Color.parseColor("#B2DFDB"),
        Color.parseColor("#C8E6C9"), Color.parseColor("#DCEDC8"), Color.parseColor("#FFF9C4"), Color.parseColor("#FFE0B2"),
        Color.parseColor("#FFCCBC"), Color.parseColor("#D7CCC8"), Color.parseColor("#CFD8DC"), Color.parseColor("#F5F5F5")
    )
    private val PALETTE_EARTH = intArrayOf(
        Color.parseColor("#C8B89A"), Color.parseColor("#D4A882"), Color.parseColor("#C4906C"), Color.parseColor("#A8A87C"),
        Color.parseColor("#8CAAC8"), Color.parseColor("#7090B0"), Color.parseColor("#94B0C8"), Color.parseColor("#607890"),
        Color.parseColor("#90B498"), Color.parseColor("#789870"), Color.parseColor("#A4C0A0"), Color.parseColor("#7A9A6C"),
        Color.parseColor("#C8B870"), Color.parseColor("#D0C468"), Color.parseColor("#C49498"), Color.parseColor("#B88090"),
        Color.parseColor("#A890B8"), Color.parseColor("#9480A8"), Color.parseColor("#B4A0C4"), Color.parseColor("#C0A0B4")
    )
    private val PALETTE_VIVID = intArrayOf(
        Color.parseColor("#EF5350"), Color.parseColor("#E91E63"), Color.parseColor("#FF4081"), Color.parseColor("#FF8A65"),
        Color.parseColor("#AB47BC"), Color.parseColor("#7E57C2"), Color.parseColor("#5C6BC0"), Color.parseColor("#2196F3"),
        Color.parseColor("#00BCD4"), Color.parseColor("#26A69A"), Color.parseColor("#4CAF50"), Color.parseColor("#8BC34A"),
        Color.parseColor("#CDDC39"), Color.parseColor("#FFC107"), Color.parseColor("#FF9800"), Color.parseColor("#FF5722"),
        Color.parseColor("#F06292"), Color.parseColor("#CE93D8"), Color.parseColor("#80CBC4"), Color.parseColor("#FFD54F")
    )
    private val PALETTE_BLUE = intArrayOf(
        Color.parseColor("#E3F2FD"), Color.parseColor("#B3E5FC"), Color.parseColor("#BBDEFB"), Color.parseColor("#90CAF9"),
        Color.parseColor("#81D4FA"), Color.parseColor("#64B5F6"), Color.parseColor("#4FC3F7"), Color.parseColor("#42A5F5"),
        Color.parseColor("#29B6F6"), Color.parseColor("#2196F3"), Color.parseColor("#1E88E5"), Color.parseColor("#039BE5"),
        Color.parseColor("#1976D2"), Color.parseColor("#0288D1"), Color.parseColor("#1565C0"), Color.parseColor("#7986CB"),
        Color.parseColor("#5C6BC0"), Color.parseColor("#3949AB"), Color.parseColor("#0D47A1"), Color.parseColor("#283593")
    )
    private val PALETTE_GREEN = intArrayOf(
        Color.parseColor("#E8F5E9"), Color.parseColor("#CCFF90"), Color.parseColor("#C8E6C9"), Color.parseColor("#B9F6CA"),
        Color.parseColor("#B2FF59"), Color.parseColor("#A5D6A7"), Color.parseColor("#AED581"), Color.parseColor("#81C784"),
        Color.parseColor("#9CCC65"), Color.parseColor("#69F0AE"), Color.parseColor("#66BB6A"), Color.parseColor("#7CB342"),
        Color.parseColor("#4CAF50"), Color.parseColor("#43A047"), Color.parseColor("#388E3C"), Color.parseColor("#558B2F"),
        Color.parseColor("#2E7D32"), Color.parseColor("#33691E"), Color.parseColor("#1B5E20"), Color.parseColor("#194D33")
    )
    private val PALETTE_PINK = intArrayOf(
        Color.parseColor("#FCE4EC"), Color.parseColor("#FFD7E9"), Color.parseColor("#FFCDD2"), Color.parseColor("#F8BBD0"),
        Color.parseColor("#FFB3BA"), Color.parseColor("#FFADD2"), Color.parseColor("#FF80AB"), Color.parseColor("#FF87B2"),
        Color.parseColor("#E8A0BF"), Color.parseColor("#F48FB1"), Color.parseColor("#FF6F91"), Color.parseColor("#FF4081"),
        Color.parseColor("#F06292"), Color.parseColor("#EC407A"), Color.parseColor("#E91E63"), Color.parseColor("#D81B60"),
        Color.parseColor("#C2185B"), Color.parseColor("#AD1457"), Color.parseColor("#880E4F"), Color.parseColor("#4A0028")
    )
    private val PALETTE_NAMES = arrayOf("パステル", "アース", "ビビッド", "ブルー", "グリーン", "ピンク", "マイカラー")
    private val PALETTE_DEFAULTS = arrayOf(
        PALETTE_PASTEL, PALETTE_EARTH, PALETTE_VIVID, PALETTE_BLUE, PALETTE_GREEN, PALETTE_PINK, PALETTE_PASTEL
    )
    private lateinit var palettes: Array<IntArray>
    private var activePaletteIndex = 1

    private val counts       = mutableListOf<Int>()
    private val names        = mutableListOf<String>()
    private val nameViews    = mutableListOf<TextView>()
    private val countViews   = mutableListOf<TextView>()
    private val topSections  = mutableListOf<LinearLayout>()
    private val colorIndices = mutableListOf<Int>()

    private lateinit var prefs: SharedPreferences
    private lateinit var gridContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private var totalCounters = 0
    private var cardWidth = 0
    private var draggingIndex = -1
    private val REQUEST_SPEECH     = 1001
    private val REQUEST_IMPORT_CFG = 1002
    private val REQUEST_IMPORT_CNT = 1003
    private var unit = "皿"
    private var appTitle = "西川さんお寿司カウンター"
    private lateinit var titleView: TextView
    private var quickSaveFileIdx = -1
    private var quickSaveSlotIdx = -1
    private lateinit var quickSaveDestBtn: Button
    private var countStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        prefs = getSharedPreferences("sushi_prefs", Context.MODE_PRIVATE)
        totalCounters = prefs.getInt("counter_count", 6)
        unit = prefs.getString("unit", "皿") ?: "皿"
        appTitle = prefs.getString("app_title", "西川さんお寿司カウンター") ?: "西川さんお寿司カウンター"
        activePaletteIndex = prefs.getInt("active_palette", 1)
        palettes = Array(7) { p -> IntArray(20) { c -> prefs.getInt("pal_${p}_${c}", PALETTE_DEFAULTS[p][c]) } }
        quickSaveFileIdx = prefs.getInt("quick_save_file", -1)
        quickSaveSlotIdx = prefs.getInt("quick_save_slot", -1)

        for (i in 0 until totalCounters) {
            counts.add(savedInstanceState?.getInt("count_$i", 0) ?: 0)
            val def = if (i < DEFAULT_NAMES.size) DEFAULT_NAMES[i] else "ネタ${i + 1}"
            names.add(prefs.getString("name_$i", def) ?: def)
            colorIndices.add(prefs.getInt("color_$i",
                if (i < DEFAULT_COLOR_INDICES.size) DEFAULT_COLOR_INDICES[i] else 14))
        }

        cardWidth = (resources.displayMetrics.widthPixels - dp(24)) / 3
        setContentView(buildUI())
    }

    private fun dp(v: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun activeColor(i: Int) = palettes[activePaletteIndex][i]

    private fun buildUI(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F0F0F0"))
        }

        val sbId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val topInset = if (sbId > 0) resources.getDimensionPixelSize(sbId) else dp(28)
        root.addView(View(this), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, topInset))

        val titleBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setGravity(Gravity.CENTER_VERTICAL)
        }
        titleView = TextView(this).apply {
            text = appTitle
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1976D2"))
            setGravity(Gravity.CENTER)
            maxLines = 1
            setPadding(dp(8), dp(8), dp(4), dp(8))
            setOnClickListener { showTitleEditDialog() }
        }
        titleBar.addView(titleView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val micBtn = Button(this).apply {
            text = "🎤"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setPadding(dp(4), dp(4), dp(8), dp(4))
            background = GradientDrawable().apply { setColor(Color.TRANSPARENT) }
            setOnClickListener { startVoiceInput() }
        }
        titleBar.addView(micBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(titleBar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val stepBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setGravity(Gravity.CENTER_VERTICAL)
            setPadding(dp(12), dp(4), dp(8), dp(4))
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
        val stepLabel = TextView(this).apply {
            text = "カウント単位："
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.parseColor("#555555"))
        }
        fun makeStepBtn(label: String, step: Int, btns: Array<Button?>) = Button(this).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(dp(4), 0, dp(4), 0)
            setOnClickListener {
                countStep = step
                btns.forEach { it?.background = GradientDrawable().apply {
                    setColor(Color.WHITE); setStroke(dp(1), Color.parseColor("#BBBBBB")); setCornerRadius(dp(4).toFloat())
                }}
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1976D2")); setCornerRadius(dp(4).toFloat())
                }
                setTextColor(Color.WHITE)
                btns.filter { it != this }?.forEach { it?.setTextColor(Color.parseColor("#555555")) }
            }
        }
        val stepBtns = arrayOfNulls<Button>(2)
        stepBtns[0] = makeStepBtn("×1",  1,  stepBtns)
        stepBtns[1] = makeStepBtn("×10", 10, stepBtns)
        // 初期状態：×1 を選択済みスタイルに
        stepBtns[0]!!.apply {
            background = GradientDrawable().apply { setColor(Color.parseColor("#1976D2")); setCornerRadius(dp(4).toFloat()) }
            setTextColor(Color.WHITE)
        }
        stepBtns[1]!!.apply {
            background = GradientDrawable().apply { setColor(Color.WHITE); setStroke(dp(1), Color.parseColor("#BBBBBB")); setCornerRadius(dp(4).toFloat()) }
            setTextColor(Color.parseColor("#555555"))
        }
        stepBar.addView(stepLabel)
        stepBtns.forEach { btn ->
            stepBar.addView(btn, LinearLayout.LayoutParams(dp(52), dp(28)).also { it.setMargins(dp(4), 0, dp(4), 0) })
        }
        root.addView(stepBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

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
            it.topMargin = dp(6)
        })

        scrollView.addView(inner, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        root.addView(scrollView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // 下部ボタンバー（保存先設定・上書き保存 ＋ 全リセット・メニュー）
        val quickBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        quickSaveDestBtn = Button(this).apply {
            text = quickSaveDestLabel()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(Color.parseColor("#555555"))
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#888888"))
            }
            setOnClickListener { showQuickSavePicker() }
        }
        val quickExecBtn = Button(this).apply {
            text = "上書き保存"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(Color.parseColor("#1976D2"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#1976D2"))
            }
            setOnClickListener { doQuickSave() }
        }
        quickBar.addView(quickSaveDestBtn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f))
        quickBar.addView(quickExecBtn,     LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(quickBar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val bottomBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val countResetBtn = Button(this).apply {
            text = "カウントリセット"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.parseColor("#FF6F00"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#FF6F00"))
            }
            setOnClickListener { showCountResetDialog() }
        }
        val initBtn = Button(this).apply {
            text = "初期化"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.parseColor("#F44336"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#F44336"))
            }
            setOnClickListener { showAllResetDialog() }
        }
        val menuBtn = Button(this).apply {
            text = "メニュー ▲"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.parseColor("#1976D2"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#1976D2"))
            }
        }
        menuBtn.setOnClickListener { showMenuPopup(menuBtn) }

        bottomBar.addView(countResetBtn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        bottomBar.addView(initBtn,       LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        bottomBar.addView(menuBtn,       LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(bottomBar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

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

        val topSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(activeColor(colorIndices[index]))
        }
        topSections.add(topSection)

        val nameRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val nameView = TextView(this).apply {
            text = names[index]
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#5C4033"))
            setGravity(Gravity.CENTER)
            setPadding(dp(4), dp(3), dp(4), dp(3))
            setOnClickListener { showEditDialog(index) }
            setOnLongClickListener { showColorDialog(index); true }
        }
        nameViews.add(nameView)

        val deleteBtn = Button(this).apply {
            text = "☒"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 0)
            background = GradientDrawable().apply { setColor(Color.TRANSPARENT) }
            setOnClickListener { confirmDelete(index) }
        }

        nameRow.addView(nameView,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        nameRow.addView(deleteBtn,
            LinearLayout.LayoutParams(dp(32), dp(32)))
        topSection.addView(nameRow,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT))

        val countView = TextView(this).apply {
            text = counts[index].toString()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#5C4033"))
            setGravity(Gravity.CENTER)
            setPadding(0, dp(2), 0, dp(2))
            setOnLongClickListener {
                draggingIndex = index
                val clip = android.content.ClipData.newPlainText("idx", index.toString())
                card.startDrag(clip, android.view.View.DragShadowBuilder(card), null, 0)
                true
            }
        }
        countViews.add(countView)
        topSection.addView(countView,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT))

        card.addView(topSection,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT))

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
            counts[index] += countStep; countView.text = counts[index].toString()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))
        btnLayout.addView(btn("－", "#546E7A", "#546E7A") {
            counts[index] = maxOf(0, counts[index] - countStep); countView.text = counts[index].toString()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))
        btnLayout.addView(btn("×", "#B0BEC5", "#B0BEC5") {
            counts[index] = 0; countView.text = "0"
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))

        card.addView(btnLayout,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT))

        card.setOnDragListener { _, event ->
            when (event.action) {
                android.view.DragEvent.ACTION_DRAG_ENTERED -> {
                    if (index != draggingIndex) card.alpha = 0.55f
                    true
                }
                android.view.DragEvent.ACTION_DRAG_EXITED -> {
                    card.alpha = 1.0f; true
                }
                android.view.DragEvent.ACTION_DROP -> {
                    card.alpha = 1.0f
                    val from = draggingIndex
                    if (from >= 0 && from != index) insertCounter(from, index)
                    true
                }
                android.view.DragEvent.ACTION_DRAG_ENDED -> {
                    card.alpha = 1.0f; draggingIndex = -1; true
                }
                else -> true
            }
        }

        return card
    }

    private fun insertCounter(from: Int, to: Int) {
        val c  = counts.removeAt(from)
        val n  = names.removeAt(from)
        val ci = colorIndices.removeAt(from)
        val insertAt = if (from < to) to - 1 else to
        counts.add(insertAt, c)
        names.add(insertAt, n)
        colorIndices.add(insertAt, ci)
        val editor = prefs.edit()
        for (i in 0 until totalCounters) {
            editor.putString("name_$i", names[i]).putInt("color_$i", colorIndices[i])
        }
        editor.apply()
        rebuildGrid()
    }

    private fun confirmDelete(index: Int) {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("削除")
            .setMessage("「${names[index]}」を削除しますか？")
            .setPositiveButton("削除") { _, _ ->
                counts.removeAt(index); names.removeAt(index); colorIndices.removeAt(index)
                totalCounters--
                val editor = prefs.edit().putInt("counter_count", totalCounters)
                for (i in 0 until totalCounters) {
                    editor.putString("name_$i", names[i]).putInt("color_$i", colorIndices[i])
                }
                editor.remove("name_$totalCounters").remove("color_$totalCounters").apply()
                rebuildGrid()
            }
            .setNegativeButton("キャンセル", null).show()
    }

    private fun showCountResetDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("カウントリセット")
            .setMessage("すべてのカウントを0にします。\nネタ名や設定はそのまま維持されます。")
            .setPositiveButton("リセット") { _, _ ->
                for (i in 0 until totalCounters) {
                    counts[i] = 0
                    countViews[i].text = "0"
                }
            }
            .setNegativeButton("キャンセル", null).show()
    }

    private fun showAllResetDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("全リセット")
            .setMessage("デフォルト状態に戻します。\n追加したカウンターは削除され、カウントも0になります。")
            .setPositiveButton("リセット") { _, _ ->
                val editor = prefs.edit()
                for (i in 0 until totalCounters) editor.remove("name_$i").remove("color_$i")
                editor.putInt("counter_count", 6).remove("app_title").remove("unit").remove("active_palette")
                for (p in 0 until 7) for (c in 0 until 20) editor.remove("pal_${p}_${c}")
                editor.apply()
                counts.clear(); names.clear(); colorIndices.clear()
                totalCounters = 6
                for (i in 0 until 6) {
                    counts.add(0); names.add(DEFAULT_NAMES[i])
                    colorIndices.add(DEFAULT_COLOR_INDICES[i])
                }
                appTitle = "西川さんお寿司カウンター"
                titleView.text = "西川さんお寿司カウンター"
                unit = "皿"
                activePaletteIndex = 1
                palettes = Array(7) { p -> IntArray(20) { c -> PALETTE_DEFAULTS[p][c] } }
                rebuildGrid()
            }
            .setNegativeButton("キャンセル", null).show()
    }

    private fun rebuildGrid() {
        gridContainer.removeAllViews()
        nameViews.clear(); countViews.clear(); topSections.clear()
        for (i in 0 until totalCounters) appendCard(i)
    }

    // ─── メニュー ───────────────────────────────────────────

    private fun showMenuPopup(anchor: View) {
        val items = arrayOf("テーマ変更（${PALETTE_NAMES[activePaletteIndex]}）", "棒グラフ（多い順・0含む）", "円グラフ（多い順・0除外）", "クリップボードにコピー", "単位を変更（現在：$unit）", "カウンター保存／呼出", "カウント結果保存／呼出", "設定データをエクスポート", "カウントデータをエクスポート", "設定データをインポート", "カウントデータをインポート", "マニュアル", "更新履歴")
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("メニュー")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showThemeDialog()
                    1 -> showBarChartDialog()
                    2 -> showPieChartDialog()
                    3 -> copyToClipboard()
                    4 -> showUnitEditDialog()
                    5 -> showSaveSlotDialog()
                    6 -> showCountFileDialog()
                    7 -> exportJson(false)
                    8 -> exportJson(true)
                    9 -> startImportPicker(false)
                    10 -> startImportPicker(true)
                    11 -> showManualDialog()
                    12 -> showChangelogDialog()
                }
            }
            .setNegativeButton("キャンセル", null).show()
    }

    private fun showTitleEditDialog() {
        val edit = EditText(this).apply {
            setText(appTitle)
            inputType = InputType.TYPE_CLASS_TEXT
            selectAll()
        }
        val wrap = LinearLayout(this).apply { setPadding(60, 20, 60, 20) }
        wrap.addView(edit)
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("タイトルを変更")
            .setView(wrap)
            .setPositiveButton("変更") { _, _ ->
                val t = edit.text.toString().trim()
                if (t.isNotEmpty()) {
                    appTitle = t
                    titleView.text = t
                    prefs.edit().putString("app_title", t).apply()
                }
            }
            .setNegativeButton("キャンセル", null).show()
    }

    private fun showUnitEditDialog() {
        val edit = EditText(this).apply {
            setText(unit)
            inputType = InputType.TYPE_CLASS_TEXT
            selectAll()
        }
        val wrap = LinearLayout(this).apply { setPadding(60, 20, 60, 20) }
        wrap.addView(edit)
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("単位を変更")
            .setView(wrap)
            .setPositiveButton("変更") { _, _ ->
                val u = edit.text.toString()
                if (u.isNotEmpty()) {
                    unit = u
                    prefs.edit().putString("unit", u).apply()
                }
            }
            .setNegativeButton("キャンセル", null).show()
    }

    private fun copyToClipboard() {
        val sb = StringBuilder()
        sb.appendln(appTitle)
        for (i in 0 until totalCounters) {
            sb.appendln("${names[i]}：${counts[i]}$unit")
        }
        val total = counts.sum()
        sb.append("合計：${total}$unit")
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("sushi_count", sb.toString()))
        Toast.makeText(this, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
    }

    private fun showBarChartDialog() {
        val items = (0 until totalCounters)
            .map { Triple(names[it], counts[it], activeColor(colorIndices[it])) }
            .sortedByDescending { it.second }
        val chartView = BarChartView(this, items)
        val sv = ScrollView(this).apply { setPadding(dp(4), dp(4), dp(4), dp(4)) }
        sv.addView(chartView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        wrapper.addView(sv, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        wrapper.addView(makeCopyImgBtn(chartView), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
            it.setMargins(dp(16), dp(4), dp(16), dp(8))
        })
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("棒グラフ（多い順・0含む）")
            .setView(wrapper)
            .setNegativeButton("閉じる", null).show()
    }

    private fun showPieChartDialog() {
        val items = (0 until totalCounters)
            .map { Triple(names[it], counts[it], activeColor(colorIndices[it])) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
        val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val sv = ScrollView(this).apply { setPadding(dp(4), dp(4), dp(4), dp(4)) }
        if (items.isEmpty()) {
            sv.addView(TextView(this).apply {
                text = "カウントがすべて0のため表示できません"
                setPadding(dp(16), dp(16), dp(16), dp(16))
            }, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            wrapper.addView(sv, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        } else {
            val chartView = PieChartView(this, items)
            sv.addView(chartView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            wrapper.addView(sv, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            wrapper.addView(makeCopyImgBtn(chartView), LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                it.setMargins(dp(16), dp(4), dp(16), dp(8))
            })
        }
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("円グラフ（多い順・0除外）")
            .setView(wrapper)
            .setNegativeButton("閉じる", null).show()
    }

    private fun makeCopyImgBtn(chartView: View): Button =
        Button(this).apply {
            text = "📋 画像をコピー"
            setTextColor(Color.parseColor("#1976D2"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#1976D2"))
            }
            setOnClickListener { copyChartToClipboard(chartView) }
        }

    private fun copyChartToClipboard(chartView: View) {
        if (android.os.Build.VERSION.SDK_INT < 29) {
            Toast.makeText(this, "Android 10以上で利用できます", Toast.LENGTH_SHORT).show()
            return
        }
        val w = chartView.width
        val h = chartView.height
        if (w <= 0 || h <= 0) {
            Toast.makeText(this, "グラフを表示してからコピーしてください", Toast.LENGTH_SHORT).show()
            return
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        chartView.draw(canvas)
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,
                "sushi_chart_${System.currentTimeMillis()}.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            Toast.makeText(this, "コピーに失敗しました", Toast.LENGTH_SHORT).show()
            return
        }
        contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 95, it) }
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newUri(contentResolver, "sushi_chart", uri))
        Toast.makeText(this, "グラフをコピーしました", Toast.LENGTH_SHORT).show()
    }

    private fun showManualDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("マニュアル  v1.00+261804072439")
            .setMessage(
                "■ タイトルの変更\nタイトルをタップ\n\n" +
                "■ ネタ名の変更\nネタ名をタップ\n\n" +
                "■ 背景色の変更\nネタ名を長押し\n\n" +
                "■ カードの並べ替え\n数字を長押ししてドラッグ\n移動先に割り込み、以降がひとつズレます\n\n" +
                "■ カードの削除\nカード右上の ☒ をタップ（確認あり）\n\n" +
                "■ カウント単位の切り替え\nタイトル下の ×1 / ×10 をタップ\n＋／－ボタンの増減量が切り替わります\n\n" +
                "■ カウントリセット\n下部「カウントリセット」をタップ\nカウントのみ0に戻します（ネタ名・設定はそのまま）\n\n" +
                "■ 初期化\n下部「初期化」をタップ\nデフォルト6種類に戻り、すべての設定がリセットされます\n\n" +
                "■ 保存先設定\n下部「保存先設定」をタップ\nカウント結果の上書き先ファイル・スロットを選択します\n\n" +
                "■ 上書き保存\n下部「上書き保存」をタップ\n設定した保存先にカウント結果を即時保存します\n\n" +
                "■ 音声入力\nタイトル右の 🎤 をタップ\n例：「いか いち まぐろ さん」\n\n" +
                "■ クリップボードにコピー\nメニュー →「クリップボードにコピー」をタップ\n全カウントをテキスト形式でコピーします\n\n" +
                "■ 単位の変更\nメニュー →「単位を変更」をタップ\n\n" +
                "■ カウンター保存／呼出\nメニュー →「カウンター保存／呼出」をタップ\n設定（タイトル・単位・ネタ名・パレット）を10スロットに保存\n呼び出し時のカウントはすべて0\n\n" +
                "■ カウント結果保存／呼出\nメニュー →「カウント結果保存／呼出」をタップ\n10ファイル×12スロットにカウント数も含めて保存・復元\n続きからカウントを再開できます\n\n" +
                "■ テーマ変更\nメニュー →「テーマ変更」をタップ\n7種のカラーパレットから選択\nマイカラーを長押しでパレット編集画面へ\n\n" +
                "■ カラーの個別編集（マイカラーのみ）\nネタ名を長押し → 色選択で色を長押し\nRGBスライダーで自由に色を設定できます\n※パステル〜ピンクのパレットは読み取り専用です\n\n" +
                "■ マイカラーへの一括コピー\nネタ名を長押し → 色選択の「別パレットから一括コピー」\nまたはテーマ変更でマイカラーを長押し → コピーボタン\n\n" +
                "■ データのエクスポート（JSON）\nメニュー →「設定データをエクスポート」または「カウントデータをエクスポート」\n保存スロットの内容をJSON形式で書き出します\nAndroid 10以降はダウンロードフォルダに保存後に共有シートを表示\nAndroid 9以前は共有シートのみ表示\n\n" +
                "■ データのインポート（JSON）\nメニュー →「設定データをインポート」または「カウントデータをインポート」\nエクスポートしたJSONファイルを選択すると対応するスロットに上書き復元\n異なる種類のファイルを読み込んだ場合はエラーを表示"
            )
            .setNegativeButton("閉じる", null).show()
    }

    private fun showChangelogDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("更新履歴")
            .setMessage(
                "■ v1.00  2026年4月18日〜19日\n" +
                "・正式リリース\n" +
                "・カウンター追加・削除・並べ替え\n" +
                "・音声入力（かな/漢字/カタカナ対応）\n" +
                "・カラーパレット7種（テーマ切り替え）\n" +
                "・マイカラー：RGBエディタ＋他パレットから一括コピー\n" +
                "・テーマ画面からマイカラーパレット直接編集\n" +
                "・棒グラフ・円グラフ（画像クリップボードコピー対応）\n" +
                "・タイトル・単位のカスタマイズ\n" +
                "・カウント単位 ×1 / ×10 切り替え\n" +
                "・カウントリセット／初期化 分割ボタン\n" +
                "・保存先設定＋上書き保存ボタン\n" +
                "・カウンター設定保存（10スロット）\n" +
                "・カウント結果保存（10ファイル×12スロット）\n" +
                "・保存／呼出のファイル名変更機能\n" +
                "・設定データ／カウントデータのJSONエクスポート・インポート"
            )
            .setNegativeButton("閉じる", null).show()
    }

    // ─── JSON エクスポート ────────────────────────────────────

    private fun String.jsonEscape() =
        replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")

    private fun colorToHex(c: Int) = String.format("#%06X", 0xFFFFFF and c)

    private fun paletteJson(prefix: String, slot: Int): String {
        val sb = StringBuilder("[")
        for (p in 0 until 7) {
            sb.append("[")
            for (c in 0 until 20) {
                val col = prefs.getInt("${prefix}_${slot}_pal_${p}_${c}", PALETTE_DEFAULTS[p][c])
                sb.append("\"${colorToHex(col)}\"")
                if (c < 19) sb.append(",")
            }
            sb.append("]")
            if (p < 6) sb.append(",")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun exportJson(isCountResult: Boolean) {
        val ts  = java.text.SimpleDateFormat("yyyyMMddHHmm", java.util.Locale.JAPAN).format(java.util.Date())
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.JAPAN).format(java.util.Date())
        val json: String
        val fileName: String

        if (!isCountResult) {
            // ── カウンター設定スロット ──
            val prefix = "slot_cfg"
            val slots = mutableListOf<String>()
            for (slot in 0 until SLOT_COUNT_CFG) {
                if (!prefs.getBoolean("${prefix}_${slot}_exists", false)) continue
                val n  = prefs.getInt("${prefix}_${slot}_count", 0)
                val counters = (0 until n).map { i ->
                    val nm = (prefs.getString("${prefix}_${slot}_name_$i", "") ?: "").jsonEscape()
                    val ci = prefs.getInt("${prefix}_${slot}_color_$i", 0)
                    "{\"name\":\"$nm\",\"color_index\":$ci}"
                }.joinToString(",")
                slots.add("""{
      "slot":${slot+1},
      "name":"${(prefs.getString("${prefix}_${slot}_title","") ?: "").jsonEscape()}",
      "saved_at":"${(prefs.getString("${prefix}_${slot}_saved_at","") ?: "").jsonEscape()}",
      "app_title":"${(prefs.getString("${prefix}_${slot}_app_title","") ?: "").jsonEscape()}",
      "unit":"${(prefs.getString("${prefix}_${slot}_unit","皿") ?: "皿").jsonEscape()}",
      "palette_index":${prefs.getInt("${prefix}_${slot}_palette",1)},
      "counters":[$counters],
      "palettes":${paletteJson(prefix, slot)}
    }""")
            }
            json = "{\n  \"type\":\"counter_settings\",\n  \"exported_at\":\"$now\",\n  \"slots\":[\n    ${slots.joinToString(",\n    ")}\n  ]\n}"
            fileName = "sushi_settings_$ts.json"
        } else {
            // ── カウント結果ファイル ──
            val files = mutableListOf<String>()
            for (f in 0 until CNT_FILE_COUNT) {
                val fName = (prefs.getString("cnt_file_${f}_name", "ファイル${f+1}") ?: "ファイル${f+1}").jsonEscape()
                val prefix = "cnt_f$f"
                val slots = mutableListOf<String>()
                for (slot in 0 until CNT_SLOTS_PER_FILE) {
                    if (!prefs.getBoolean("${prefix}_${slot}_exists", false)) continue
                    val n = prefs.getInt("${prefix}_${slot}_count", 0)
                    val counters = (0 until n).map { i ->
                        val nm = (prefs.getString("${prefix}_${slot}_name_$i", "") ?: "").jsonEscape()
                        val ci = prefs.getInt("${prefix}_${slot}_color_$i", 0)
                        val v  = prefs.getInt("${prefix}_${slot}_val_$i", 0)
                        "{\"name\":\"$nm\",\"color_index\":$ci,\"count\":$v}"
                    }.joinToString(",")
                    slots.add("""{
          "slot":${slot+1},
          "name":"${(prefs.getString("${prefix}_${slot}_title","") ?: "").jsonEscape()}",
          "saved_at":"${(prefs.getString("${prefix}_${slot}_saved_at","") ?: "").jsonEscape()}",
          "app_title":"${(prefs.getString("${prefix}_${slot}_app_title","") ?: "").jsonEscape()}",
          "unit":"${(prefs.getString("${prefix}_${slot}_unit","皿") ?: "皿").jsonEscape()}",
          "palette_index":${prefs.getInt("${prefix}_${slot}_palette",1)},
          "counters":[$counters],
          "palettes":${paletteJson(prefix, slot)}
        }""")
                }
                files.add("{\n      \"file\":${f+1},\n      \"file_name\":\"$fName\",\n      \"slots\":[\n        ${slots.joinToString(",\n        ")}\n      ]\n    }")
            }
            json = "{\n  \"type\":\"count_results\",\n  \"exported_at\":\"$now\",\n  \"files\":[\n    ${files.joinToString(",\n    ")}\n  ]\n}"
            fileName = "sushi_count_$ts.json"
        }

        saveAndShareJson(json, fileName)
    }

    private fun saveAndShareJson(content: String, fileName: String) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            try {
                val downloadsClass = Class.forName("android.provider.MediaStore\$Downloads")
                val displayNameField = downloadsClass.getField("DISPLAY_NAME")
                val mimeTypeField = downloadsClass.getField("MIME_TYPE")
                val extUriField = downloadsClass.getField("EXTERNAL_CONTENT_URI")
                val displayName = displayNameField.get(null) as String
                val mimeType = mimeTypeField.get(null) as String
                val extUri = extUriField.get(null) as android.net.Uri
                val values = android.content.ContentValues().apply {
                    put(displayName, fileName)
                    put(mimeType, "application/json")
                }
                val uri = contentResolver.insert(extUri, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                    android.widget.Toast.makeText(this, "ダウンロードフォルダに保存しました：$fileName", android.widget.Toast.LENGTH_LONG).show()
                    val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(android.content.Intent.createChooser(share, "エクスポート：$fileName"))
                    return
                }
            } catch (_: Exception) { }
        }
        // フォールバック：テキスト共有
        val share = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, content)
            putExtra(android.content.Intent.EXTRA_SUBJECT, fileName)
        }
        startActivity(android.content.Intent.createChooser(share, "エクスポート：$fileName"))
    }

    // ─── クイック上書き保存 ───────────────────────────────────

    private fun quickSaveDestLabel(): String {
        if (quickSaveFileIdx < 0 || quickSaveSlotIdx < 0) return "保存先設定（未設定）"
        val fileName = prefs.getString("cnt_file_${quickSaveFileIdx}_name", "ファイル${quickSaveFileIdx + 1}") ?: "ファイル${quickSaveFileIdx + 1}"
        return "保存先：$fileName / スロット${quickSaveSlotIdx + 1}"
    }

    private fun showQuickSavePicker() {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)
        for (f in 0 until CNT_FILE_COUNT) {
            val fileName = prefs.getString("cnt_file_${f}_name", "ファイル${f + 1}") ?: "ファイル${f + 1}"
            val usedCount = (0 until CNT_SLOTS_PER_FILE).count { prefs.getBoolean("cnt_f${f}_${it}_exists", false) }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setGravity(Gravity.CENTER_VERTICAL)
                setPadding(dp(12), dp(12), dp(12), dp(12))
                setOnClickListener {
                    dialogHolder[0]?.dismiss()
                    showQuickSaveSlotPicker(f)
                }
            }
            row.addView(TextView(this).apply {
                text = fileName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.parseColor("#333333"))
                setTypeface(typeface, Typeface.BOLD)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(TextView(this).apply {
                text = "$usedCount / $CNT_SLOTS_PER_FILE 件  ▶"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(Color.parseColor("#888888"))
            })
            wrapper.addView(row)
            if (f < CNT_FILE_COUNT - 1)
                wrapper.addView(View(this).apply { setBackgroundColor(Color.parseColor("#EEEEEE")) },
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1))
        }
        val sv = ScrollView(this).apply { addView(wrapper) }
        dialogHolder[0] = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("保存先ファイルを選択")
            .setView(sv)
            .setNegativeButton("キャンセル", null).show()
    }

    private fun showQuickSaveSlotPicker(fileIdx: Int) {
        val fileName = prefs.getString("cnt_file_${fileIdx}_name", "ファイル${fileIdx + 1}") ?: "ファイル${fileIdx + 1}"
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)
        for (s in 0 until CNT_SLOTS_PER_FILE) {
            val exists = prefs.getBoolean("cnt_f${fileIdx}_${s}_exists", false)
            val slotName = prefs.getString("cnt_f${fileIdx}_${s}_title", "") ?: ""
            val savedAt  = prefs.getString("cnt_f${fileIdx}_${s}_saved_at", "") ?: ""
            val isSelected = fileIdx == quickSaveFileIdx && s == quickSaveSlotIdx
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                if (isSelected) background = GradientDrawable().apply {
                    setColor(Color.parseColor("#E3F2FD"))
                    setStroke(dp(2), Color.parseColor("#1976D2"))
                    setCornerRadius(dp(4).toFloat())
                }
                setOnClickListener {
                    quickSaveFileIdx = fileIdx
                    quickSaveSlotIdx = s
                    prefs.edit().putInt("quick_save_file", fileIdx).putInt("quick_save_slot", s).apply()
                    quickSaveDestBtn.text = quickSaveDestLabel()
                    dialogHolder[0]?.dismiss()
                    Toast.makeText(this@MainActivity, "保存先を設定しました", Toast.LENGTH_SHORT).show()
                }
            }
            row.addView(TextView(this).apply {
                text = "スロット ${s + 1}" + if (isSelected) "  ✓" else ""
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(if (isSelected) Color.parseColor("#1976D2") else Color.parseColor("#999999"))
            })
            row.addView(TextView(this).apply {
                text = if (exists) slotName else "（空き）"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(if (exists) Color.parseColor("#333333") else Color.LTGRAY)
                setTypeface(typeface, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            })
            if (exists) row.addView(TextView(this).apply {
                text = savedAt
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(Color.parseColor("#AAAAAA"))
            })
            wrapper.addView(row)
            if (s < CNT_SLOTS_PER_FILE - 1)
                wrapper.addView(View(this).apply { setBackgroundColor(Color.parseColor("#EEEEEE")) },
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1))
        }
        val sv = ScrollView(this).apply { addView(wrapper) }
        dialogHolder[0] = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("$fileName　スロットを選択")
            .setView(sv)
            .setNegativeButton("キャンセル", null).show()
    }

    private fun doQuickSave() {
        if (quickSaveFileIdx < 0 || quickSaveSlotIdx < 0) {
            Toast.makeText(this, "先に「保存先設定」で保存先を選んでください", Toast.LENGTH_SHORT).show()
            return
        }
        val fileName = prefs.getString("cnt_file_${quickSaveFileIdx}_name", "ファイル${quickSaveFileIdx + 1}") ?: "ファイル${quickSaveFileIdx + 1}"
        val slotNo = quickSaveSlotIdx + 1
        val prefix = "cnt_f${quickSaveFileIdx}"
        val existingName = prefs.getString("${prefix}_${quickSaveSlotIdx}_title", "") ?: ""
        val msg = if (existingName.isNotEmpty())
            "$fileName / スロット$slotNo「$existingName」に上書き保存しますか？"
        else
            "$fileName / スロット${slotNo}（空き）に保存しますか？"
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("上書き保存の確認")
            .setMessage(msg)
            .setPositiveButton("保存") { _, _ ->
                val saveName = existingName.ifEmpty {
                    appTitle + "_" + java.text.SimpleDateFormat("yyyyMMddHHmm", java.util.Locale.JAPAN).format(java.util.Date())
                }
                saveToSlot(prefix, quickSaveSlotIdx, true, saveName)
            }
            .setNegativeButton("キャンセル") { _, _ ->
                Toast.makeText(this, "保存しませんでした！", Toast.LENGTH_SHORT).show()
            }.show()
    }

    // ─── 保存／呼出 ──────────────────────────────────────────

    private val SLOT_COUNT_CFG    = 10
    private val CNT_FILE_COUNT    = 10
    private val CNT_SLOTS_PER_FILE = 12

    // ── カウンター設定：10スロット ──────────────────────────
    private fun showSaveSlotDialog() {
        buildSlotListDialog(
            prefix = "slot_cfg",
            slotCount = SLOT_COUNT_CFG,
            dialogTitle = "カウンター設定 保存／呼出",
            withCounts = false,
            onRefresh = { showSaveSlotDialog() }
        )
    }

    // ── カウント結果：ファイル一覧 ──────────────────────────
    private fun showCountFileDialog() {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)

        for (f in 0 until CNT_FILE_COUNT) {
            val fileName = prefs.getString("cnt_file_${f}_name", "ファイル${f + 1}") ?: "ファイル${f + 1}"
            val usedCount = (0 until CNT_SLOTS_PER_FILE).count {
                prefs.getBoolean("cnt_f${f}_${it}_exists", false)
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setGravity(Gravity.CENTER_VERTICAL)
                setPadding(dp(4), dp(10), dp(4), dp(10))
            }
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            info.addView(TextView(this).apply {
                text = fileName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(Color.parseColor("#333333"))
                setTypeface(typeface, Typeface.BOLD)
            })
            info.addView(TextView(this).apply {
                text = "$usedCount / $CNT_SLOTS_PER_FILE 件"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(Color.parseColor("#999999"))
            })

            fun mkBtn(label: String, color: String, action: () -> Unit) =
                Button(this).apply {
                    text = label; setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(Color.parseColor(color)); setPadding(0, 0, 0, 0)
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE); setStroke(dp(1), Color.parseColor(color))
                        setCornerRadius(dp(4).toFloat())
                    }
                    setOnClickListener { action() }
                }

            val openBtn = mkBtn("開く", "#388E3C") {
                dialogHolder[0]?.dismiss()
                showCountFileSlots(f)
            }
            val renameBtn = mkBtn("名前変更", "#1976D2") {
                val edit = EditText(this).apply {
                    setText(fileName); inputType = InputType.TYPE_CLASS_TEXT; selectAll()
                }
                val wrap = LinearLayout(this).apply { setPadding(dp(16), dp(8), dp(16), dp(8)) }
                wrap.addView(edit)
                AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle("ファイル名を変更")
                    .setView(wrap)
                    .setPositiveButton("変更") { _, _ ->
                        val n = edit.text.toString().trim().ifEmpty { "ファイル${f + 1}" }
                        prefs.edit().putString("cnt_file_${f}_name", n).apply()
                        dialogHolder[0]?.dismiss(); showCountFileDialog()
                    }
                    .setNegativeButton("キャンセル", null).show()
            }

            row.addView(info, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(renameBtn, LinearLayout.LayoutParams(dp(72), dp(38)).also { it.setMargins(dp(4), 0, dp(4), 0) })
            row.addView(openBtn,   LinearLayout.LayoutParams(dp(54), dp(38)))
            wrapper.addView(row)
            if (f < CNT_FILE_COUNT - 1)
                wrapper.addView(View(this).apply { setBackgroundColor(Color.parseColor("#EEEEEE")) },
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1))
        }

        val sv = ScrollView(this).apply { addView(wrapper) }
        dialogHolder[0] = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("カウント結果 保存／呼出")
            .setView(sv)
            .setNegativeButton("閉じる", null).show()
    }

    // ── カウント結果：ファイル内スロット（12個）──────────────
    private fun showCountFileSlots(fileIdx: Int) {
        val fileName = prefs.getString("cnt_file_${fileIdx}_name", "ファイル${fileIdx + 1}") ?: "ファイル${fileIdx + 1}"
        buildSlotListDialog(
            prefix = "cnt_f${fileIdx}",
            slotCount = CNT_SLOTS_PER_FILE,
            dialogTitle = fileName,
            withCounts = true,
            onRefresh = { showCountFileSlots(fileIdx) }
        )
    }

    // ── 共通スロット一覧ダイアログビルダー ──────────────────
    private fun buildSlotListDialog(
        prefix: String, slotCount: Int, dialogTitle: String,
        withCounts: Boolean, onRefresh: () -> Unit
    ) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)

        for (slot in 0 until slotCount) {
            val exists    = prefs.getBoolean("${prefix}_${slot}_exists", false)
            val savedTitle = prefs.getString("${prefix}_${slot}_title", "") ?: ""
            val savedAt   = prefs.getString("${prefix}_${slot}_saved_at", "") ?: ""

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setGravity(Gravity.CENTER_VERTICAL)
                setPadding(dp(4), dp(8), dp(4), dp(8))
            }
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            info.addView(TextView(this).apply {
                text = "スロット ${slot + 1}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(Color.parseColor("#999999"))
            })
            info.addView(TextView(this).apply {
                text = if (exists) savedTitle else "（空き）"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(if (exists) Color.parseColor("#333333") else Color.LTGRAY)
            })
            if (exists) info.addView(TextView(this).apply {
                text = savedAt
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(Color.parseColor("#AAAAAA"))
            })

            fun mkBtn(label: String, color: String, enabled: Boolean, action: () -> Unit) =
                Button(this).apply {
                    text = label; setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    isEnabled = enabled
                    val c = if (enabled) Color.parseColor(color) else Color.LTGRAY
                    setTextColor(c); setPadding(0, 0, 0, 0)
                    background = GradientDrawable().apply {
                        setColor(Color.WHITE); setStroke(dp(1), c)
                        setCornerRadius(dp(4).toFloat())
                    }
                    setOnClickListener { action() }
                }

            val saveBtn = mkBtn(if (exists) "上書き" else "保存", "#1976D2", true) {
                if (exists) {
                    AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                        .setTitle("上書き確認")
                        .setMessage("スロット${slot + 1}「$savedTitle」に上書きしますか？")
                        .setPositiveButton("上書き") { _, _ ->
                            saveToSlot(prefix, slot, withCounts, savedTitle)
                            dialogHolder[0]?.dismiss(); onRefresh()
                        }
                        .setNegativeButton("キャンセル", null).show()
                } else {
                    val def = appTitle + "_" + java.text.SimpleDateFormat("yyyyMMddHHmm", java.util.Locale.JAPAN).format(java.util.Date())
                    val edit = EditText(this).apply { setText(def); inputType = InputType.TYPE_CLASS_TEXT; selectAll() }
                    val wrap = LinearLayout(this).apply { setPadding(dp(16), dp(8), dp(16), dp(8)) }
                    wrap.addView(edit)
                    AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                        .setTitle("保存名を入力").setView(wrap)
                        .setPositiveButton("保存") { _, _ ->
                            saveToSlot(prefix, slot, withCounts, edit.text.toString().trim().ifEmpty { def })
                            dialogHolder[0]?.dismiss(); onRefresh()
                        }
                        .setNegativeButton("キャンセル", null).show()
                }
            }
            val loadBtn = mkBtn("呼出", "#388E3C", exists) {
                AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle("呼出確認")
                    .setMessage("スロット${slot + 1}「$savedTitle」を呼び出しますか？\n現在のデータは上書きされます。")
                    .setPositiveButton("呼出") { _, _ ->
                        loadFromSlot(prefix, slot, withCounts); dialogHolder[0]?.dismiss()
                    }
                    .setNegativeButton("キャンセル", null).show()
            }
            val delBtn = mkBtn("削除", "#E53935", exists) {
                AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle("削除確認")
                    .setMessage("スロット${slot + 1}「$savedTitle」を削除しますか？")
                    .setPositiveButton("削除") { _, _ ->
                        deleteSlot(prefix, slot); dialogHolder[0]?.dismiss(); onRefresh()
                    }
                    .setNegativeButton("キャンセル", null).show()
            }

            row.addView(info,    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(saveBtn, LinearLayout.LayoutParams(dp(54), dp(38)).also { it.setMargins(dp(3), 0, dp(3), 0) })
            row.addView(loadBtn, LinearLayout.LayoutParams(dp(54), dp(38)).also { it.setMargins(0, 0, dp(3), 0) })
            row.addView(delBtn,  LinearLayout.LayoutParams(dp(46), dp(38)))
            wrapper.addView(row)
            if (slot < slotCount - 1)
                wrapper.addView(View(this).apply { setBackgroundColor(Color.parseColor("#EEEEEE")) },
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1))
        }

        val sv = ScrollView(this).apply { addView(wrapper) }
        dialogHolder[0] = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle(dialogTitle).setView(sv)
            .setNegativeButton("閉じる", null).show()
    }

    private fun saveToSlot(prefix: String, slot: Int, withCounts: Boolean, saveName: String) {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.JAPAN)
        val now = sdf.format(java.util.Date())
        val editor = prefs.edit()
        editor.putBoolean("${prefix}_${slot}_exists", true)
        editor.putString("${prefix}_${slot}_title", saveName)
        editor.putString("${prefix}_${slot}_app_title", appTitle)
        editor.putString("${prefix}_${slot}_unit", unit)
        editor.putString("${prefix}_${slot}_saved_at", now)
        editor.putInt("${prefix}_${slot}_count", totalCounters)
        editor.putInt("${prefix}_${slot}_palette", activePaletteIndex)
        for (i in 0 until totalCounters) {
            editor.putString("${prefix}_${slot}_name_$i", names[i])
            editor.putInt("${prefix}_${slot}_color_$i", colorIndices[i])
            if (withCounts) editor.putInt("${prefix}_${slot}_val_$i", counts[i])
        }
        for (p in 0 until 7) for (c in 0 until 20) {
            editor.putInt("${prefix}_${slot}_pal_${p}_${c}", palettes[p][c])
        }
        editor.apply()
        Toast.makeText(this, "スロット${slot + 1}に保存しました", Toast.LENGTH_SHORT).show()
    }

    private fun loadFromSlot(prefix: String, slot: Int, withCounts: Boolean) {
        val n = prefs.getInt("${prefix}_${slot}_count", 6)
        appTitle = prefs.getString("${prefix}_${slot}_app_title", "西川さんお寿司カウンター") ?: "西川さんお寿司カウンター"
        unit = prefs.getString("${prefix}_${slot}_unit", "皿") ?: "皿"
        activePaletteIndex = prefs.getInt("${prefix}_${slot}_palette", 1)
        palettes = Array(7) { p -> IntArray(20) { c ->
            prefs.getInt("${prefix}_${slot}_pal_${p}_${c}", PALETTE_DEFAULTS[p][c])
        }}
        counts.clear(); names.clear(); colorIndices.clear()
        totalCounters = n
        for (i in 0 until n) {
            names.add(prefs.getString("${prefix}_${slot}_name_$i", "ネタ${i + 1}") ?: "ネタ${i + 1}")
            colorIndices.add(prefs.getInt("${prefix}_${slot}_color_$i", 14))
            counts.add(if (withCounts) prefs.getInt("${prefix}_${slot}_val_$i", 0) else 0)
        }
        titleView.text = appTitle
        val editor = prefs.edit()
        editor.putString("app_title", appTitle).putString("unit", unit)
            .putInt("active_palette", activePaletteIndex).putInt("counter_count", totalCounters)
        for (i in 0 until totalCounters) {
            editor.putString("name_$i", names[i]).putInt("color_$i", colorIndices[i])
        }
        for (p in 0 until 7) for (c in 0 until 20) {
            editor.putInt("pal_${p}_${c}", palettes[p][c])
        }
        editor.apply()
        rebuildGrid()
        val msg = if (withCounts) "スロット${slot + 1}を呼び出しました（カウント復元）"
                  else "スロット${slot + 1}を呼び出しました（カウントは0）"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun deleteSlot(prefix: String, slot: Int) {
        val editor = prefs.edit()
        val n = prefs.getInt("${prefix}_${slot}_count", 6)
        editor.remove("${prefix}_${slot}_exists")
            .remove("${prefix}_${slot}_title").remove("${prefix}_${slot}_unit")
            .remove("${prefix}_${slot}_saved_at").remove("${prefix}_${slot}_count")
            .remove("${prefix}_${slot}_palette")
        for (i in 0 until n) {
            editor.remove("${prefix}_${slot}_name_$i")
                .remove("${prefix}_${slot}_color_$i")
                .remove("${prefix}_${slot}_val_$i")
        }
        for (p in 0 until 7) for (c in 0 until 20) {
            editor.remove("${prefix}_${slot}_pal_${p}_${c}")
        }
        editor.apply()
        Toast.makeText(this, "スロット${slot + 1}を削除しました", Toast.LENGTH_SHORT).show()
    }

    // ─── 色選択ダイアログ ─────────────────────────────────────

    private fun showColorDialog(index: Int) {
        val isMyColor = activePaletteIndex == 6
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)

        if (isMyColor) {
            val copyBtn = Button(this).apply {
                text = "📋 別パレットから一括コピー"
                setTextColor(Color.parseColor("#1976D2"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    setStroke(dp(1), Color.parseColor("#1976D2"))
                    setCornerRadius(dp(4).toFloat())
                }
                setOnClickListener {
                    val srcNames = PALETTE_NAMES.take(6).toTypedArray()
                    AlertDialog.Builder(this@MainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
                        .setTitle("コピー元を選択")
                        .setItems(srcNames) { _, which ->
                            val editor = prefs.edit()
                            for (c in 0..19) {
                                palettes[6][c] = palettes[which][c]
                                editor.putInt("pal_6_$c", palettes[6][c])
                            }
                            editor.apply()
                            dialogHolder[0]?.dismiss()
                            showColorDialog(index)
                        }
                        .setNegativeButton("キャンセル", null).show()
                }
            }
            container.addView(copyBtn, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                it.bottomMargin = dp(8)
            })
        }

        for (row in 0..4) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setGravity(Gravity.CENTER)
            }
            for (col in 0..3) {
                val ci = row * 4 + col
                val isSelected = ci == colorIndices[index]
                val swatchBg = GradientDrawable().apply {
                    setColor(activeColor(ci))
                    setStroke(dp(if (isSelected) 4 else 2),
                        if (isSelected) Color.parseColor("#1976D2") else Color.LTGRAY)
                    setCornerRadius(dp(8).toFloat())
                }
                val swatch = View(this)
                swatch.background = swatchBg
                swatch.setOnClickListener {
                    colorIndices[index] = ci
                    topSections[index].setBackgroundColor(activeColor(ci))
                    prefs.edit().putInt("color_$index", ci).apply()
                    dialogHolder[0]?.dismiss()
                }
                if (isMyColor) {
                    swatch.setOnLongClickListener {
                        showRGBEditor(activePaletteIndex, ci) {
                            swatchBg.setColor(activeColor(ci))
                            if (ci == colorIndices[index])
                                topSections[index].setBackgroundColor(activeColor(ci))
                        }
                        true
                    }
                }
                rowLayout.addView(swatch,
                    LinearLayout.LayoutParams(dp(52), dp(52)).also {
                        it.setMargins(dp(6), dp(6), dp(6), dp(6))
                    })
            }
            container.addView(rowLayout)
        }

        val title = if (isMyColor) "色を選択（長押しで色を編集）" else "色を選択（${PALETTE_NAMES[activePaletteIndex]}）"
        dialogHolder[0] = AlertDialog.Builder(
            this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle(title)
            .setView(container)
            .setNegativeButton("キャンセル", null).show()
    }

    // ─── テーマ選択ダイアログ ─────────────────────────────────

    private fun showThemeDialog() {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)

        PALETTE_NAMES.forEachIndexed { idx, name ->
            val isActive = idx == activePaletteIndex
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setGravity(Gravity.CENTER_VERTICAL)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                if (isActive) {
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#E3F2FD"))
                        setStroke(dp(2), Color.parseColor("#1976D2"))
                        setCornerRadius(dp(6).toFloat())
                    }
                }
            }
            // 5色サンプル (各行の代表色)
            val swatchRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (si in intArrayOf(0, 4, 8, 12, 16)) {
                val s = View(this)
                s.background = GradientDrawable().apply {
                    setColor(palettes[idx][si])
                    setCornerRadius(dp(4).toFloat())
                }
                swatchRow.addView(s, LinearLayout.LayoutParams(dp(18), dp(18)).also {
                    it.setMargins(dp(2), 0, dp(2), 0)
                })
            }
            val nameView = TextView(this).apply {
                text = name
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(if (isActive) Color.parseColor("#1976D2") else Color.parseColor("#333333"))
                setTypeface(typeface, if (isActive) Typeface.BOLD else Typeface.NORMAL)
                setPadding(dp(12), 0, 0, 0)
            }
            row.addView(swatchRow, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            row.addView(nameView, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            if (isActive) {
                row.addView(TextView(this).apply {
                    text = "✓"
                    setTextColor(Color.parseColor("#1976D2"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                }, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
            row.setOnClickListener {
                activePaletteIndex = idx
                prefs.edit().putInt("active_palette", idx).apply()
                rebuildGrid()
                dialogHolder[0]?.dismiss()
            }
            if (idx == 6) {
                row.setOnLongClickListener {
                    dialogHolder[0]?.dismiss()
                    showMyColorPaletteEditor()
                    true
                }
                row.addView(TextView(this).apply {
                    text = "長押しで編集"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTextColor(Color.parseColor("#AAAAAA"))
                    setPadding(dp(4), 0, dp(4), 0)
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
            wrapper.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                it.bottomMargin = dp(4)
            })
        }

        dialogHolder[0] = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("テーマ（カラーパレット）を選択")
            .setView(wrapper)
            .setNegativeButton("閉じる", null).show()
    }

    // ─── マイカラーパレット編集 ───────────────────────────────

    private fun showMyColorPaletteEditor() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)
        val swatchBgs = arrayOfNulls<GradientDrawable>(20)
        var selectedCi = -1

        fun updateBorders() {
            for (i in 0..19) {
                swatchBgs[i]?.setStroke(
                    dp(if (i == selectedCi) 4 else 2),
                    if (i == selectedCi) Color.parseColor("#1976D2") else Color.LTGRAY
                )
            }
        }

        // 別パレットから一括コピー
        val copyBtn = Button(this).apply {
            text = "📋 別パレットから一括コピー"
            setTextColor(Color.parseColor("#1976D2"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(1), Color.parseColor("#1976D2"))
                setCornerRadius(dp(4).toFloat())
            }
            setOnClickListener {
                val srcNames = PALETTE_NAMES.take(6).toTypedArray()
                AlertDialog.Builder(this@MainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle("コピー元を選択")
                    .setItems(srcNames) { _, which ->
                        val editor = prefs.edit()
                        for (c in 0..19) {
                            palettes[6][c] = palettes[which][c]
                            editor.putInt("pal_6_$c", palettes[6][c])
                            swatchBgs[c]?.setColor(palettes[6][c])
                        }
                        editor.apply()
                        selectedCi = -1
                        updateBorders()
                    }
                    .setNegativeButton("キャンセル", null).show()
            }
        }
        container.addView(copyBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
            it.bottomMargin = dp(8)
        })

        for (row in 0..4) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setGravity(Gravity.CENTER)
            }
            for (col in 0..3) {
                val ci = row * 4 + col
                val swatchBg = GradientDrawable().apply {
                    setColor(palettes[6][ci])
                    setStroke(dp(2), Color.LTGRAY)
                    setCornerRadius(dp(8).toFloat())
                }
                swatchBgs[ci] = swatchBg
                val swatch = View(this)
                swatch.background = swatchBg
                swatch.setOnClickListener {
                    selectedCi = ci
                    updateBorders()
                    showRGBEditor(6, ci) {
                        swatchBg.setColor(palettes[6][ci])
                    }
                }
                rowLayout.addView(swatch,
                    LinearLayout.LayoutParams(dp(52), dp(52)).also {
                        it.setMargins(dp(6), dp(6), dp(6), dp(6))
                    })
            }
            container.addView(rowLayout)
        }

        val customTitle = TextView(this).apply {
            text = "マイカラー編集（長押しで色を編集）"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.parseColor("#212121"))
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 1
            setPadding(dp(20), dp(16), dp(20), dp(8))
        }
        dialogHolder[0] = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setCustomTitle(customTitle)
            .setView(container)
            .setNegativeButton("閉じる", null).show()
    }

    // ─── RGB カラーエディタ ───────────────────────────────────

    private fun showRGBEditor(paletteIdx: Int, colorPos: Int, onSaved: () -> Unit) {
        val initial = palettes[paletteIdx][colorPos]
        var r = Color.red(initial)
        var g = Color.green(initial)
        var b = Color.blue(initial)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(8))
        }

        val previewBg = GradientDrawable().apply {
            setColor(Color.rgb(r, g, b))
            setCornerRadius(dp(8).toFloat())
        }
        val preview = View(this)
        preview.background = previewBg
        layout.addView(preview, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(60)).also { it.bottomMargin = dp(16) })

        var updating = false
        fun refresh() { previewBg.setColor(Color.rgb(r, g, b)) }

        // R行
        val rowR = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setGravity(Gravity.CENTER_VERTICAL) }
        val tvR = TextView(this).apply { text = "R"; setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f); setTextColor(Color.parseColor("#E53935")) }
        val seekR = SeekBar(this).apply { max = 255; progress = r }
        val editR = EditText(this).apply { setText(r.toString()); inputType = InputType.TYPE_CLASS_NUMBER; setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f) }
        seekR.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                if (!fromUser || updating) return; updating = true; r = p; editR.setText(p.toString()); refresh(); updating = false
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
        editR.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence, st: Int, c: Int, af: Int) {}
            override fun onTextChanged(s: CharSequence, st: Int, bc: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable) {
                if (updating) return; val v = s.toString().toIntOrNull()?.coerceIn(0, 255) ?: return
                updating = true; r = v; seekR.progress = v; refresh(); updating = false
            }
        })
        rowR.addView(tvR, LinearLayout.LayoutParams(dp(20), ViewGroup.LayoutParams.WRAP_CONTENT))
        rowR.addView(seekR, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        rowR.addView(editR, LinearLayout.LayoutParams(dp(55), ViewGroup.LayoutParams.WRAP_CONTENT))
        layout.addView(rowR, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = dp(8) })

        // G行
        val rowG = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setGravity(Gravity.CENTER_VERTICAL) }
        val tvG = TextView(this).apply { text = "G"; setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f); setTextColor(Color.parseColor("#43A047")) }
        val seekG = SeekBar(this).apply { max = 255; progress = g }
        val editG = EditText(this).apply { setText(g.toString()); inputType = InputType.TYPE_CLASS_NUMBER; setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f) }
        seekG.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                if (!fromUser || updating) return; updating = true; g = p; editG.setText(p.toString()); refresh(); updating = false
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
        editG.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence, st: Int, c: Int, af: Int) {}
            override fun onTextChanged(s: CharSequence, st: Int, bc: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable) {
                if (updating) return; val v = s.toString().toIntOrNull()?.coerceIn(0, 255) ?: return
                updating = true; g = v; seekG.progress = v; refresh(); updating = false
            }
        })
        rowG.addView(tvG, LinearLayout.LayoutParams(dp(20), ViewGroup.LayoutParams.WRAP_CONTENT))
        rowG.addView(seekG, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        rowG.addView(editG, LinearLayout.LayoutParams(dp(55), ViewGroup.LayoutParams.WRAP_CONTENT))
        layout.addView(rowG, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = dp(8) })

        // B行
        val rowB = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setGravity(Gravity.CENTER_VERTICAL) }
        val tvB = TextView(this).apply { text = "B"; setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f); setTextColor(Color.parseColor("#1976D2")) }
        val seekB = SeekBar(this).apply { max = 255; progress = b }
        val editB = EditText(this).apply { setText(b.toString()); inputType = InputType.TYPE_CLASS_NUMBER; setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f) }
        seekB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                if (!fromUser || updating) return; updating = true; b = p; editB.setText(p.toString()); refresh(); updating = false
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
        editB.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence, st: Int, c: Int, af: Int) {}
            override fun onTextChanged(s: CharSequence, st: Int, bc: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable) {
                if (updating) return; val v = s.toString().toIntOrNull()?.coerceIn(0, 255) ?: return
                updating = true; b = v; seekB.progress = v; refresh(); updating = false
            }
        })
        rowB.addView(tvB, LinearLayout.LayoutParams(dp(20), ViewGroup.LayoutParams.WRAP_CONTENT))
        rowB.addView(seekB, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        rowB.addView(editB, LinearLayout.LayoutParams(dp(55), ViewGroup.LayoutParams.WRAP_CONTENT))
        layout.addView(rowB, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("色を編集（${PALETTE_NAMES[paletteIdx]}）")
            .setView(layout)
            .setPositiveButton("保存") { _, _ ->
                val newColor = Color.rgb(r, g, b)
                palettes[paletteIdx][colorPos] = newColor
                prefs.edit().putInt("pal_${paletteIdx}_${colorPos}", newColor).apply()
                onSaved()
            }
            .setNegativeButton("キャンセル", null).show()
    }

    // ─── 追加フォーム ─────────────────────────────────────────

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
                counts.add(0); names.add(name); colorIndices.add(14)
                prefs.edit()
                    .putString("name_$idx", name)
                    .putInt("counter_count", totalCounters + 1)
                    .putInt("color_$idx", 14)
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
                    names[index] = name; nameViews[index].text = name
                    prefs.edit().putString("name_$index", name).apply()
                }
            }
            .setNegativeButton("キャンセル", null).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in 0 until totalCounters) outState.putInt("count_$i", counts[i])
    }

    // ─── 音声入力 ─────────────────────────────────────────────

    private fun startVoiceInput() {
        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ja-JP")
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "「ネタ名 数字」と話してください")
            putExtra(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            startActivityForResult(intent, REQUEST_SPEECH)
        } catch (e: Exception) {
            Toast.makeText(this, "音声認識が利用できません", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SPEECH && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val text = results?.firstOrNull() ?: return
            parseVoiceCommand(text)
            return
        }
        if (resultCode != Activity.RESULT_OK || data?.data == null) return
        val uri = data.data!!
        val content = try {
            contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
        } catch (e: Exception) { null }
        if (content == null) {
            android.widget.Toast.makeText(this, "ファイルを読み込めませんでした", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        when (requestCode) {
            REQUEST_IMPORT_CFG -> importJson(content, false)
            REQUEST_IMPORT_CNT -> importJson(content, true)
        }
    }

    private fun startImportPicker(isCountResult: Boolean) {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            android.content.Intent.createChooser(intent, "JSONファイルを選択"),
            if (isCountResult) REQUEST_IMPORT_CNT else REQUEST_IMPORT_CFG
        )
    }

    private fun importJson(content: String, isCountResult: Boolean) {
        try {
            val root = org.json.JSONObject(content)
            val type = root.getString("type")
            if (!isCountResult && type == "counter_settings") {
                val slots = root.getJSONArray("slots")
                var count = 0
                for (i in 0 until slots.length()) {
                    val slot = slots.getJSONObject(i)
                    val slotIdx = slot.getInt("slot") - 1
                    if (slotIdx < 0 || slotIdx >= SLOT_COUNT_CFG) continue
                    val prefix = "slot_cfg"
                    val edit = prefs.edit()
                    edit.putBoolean("${prefix}_${slotIdx}_exists", true)
                    edit.putString("${prefix}_${slotIdx}_title", slot.getString("name"))
                    edit.putString("${prefix}_${slotIdx}_saved_at", slot.getString("saved_at"))
                    edit.putString("${prefix}_${slotIdx}_app_title", slot.getString("app_title"))
                    edit.putString("${prefix}_${slotIdx}_unit", slot.getString("unit"))
                    edit.putInt("${prefix}_${slotIdx}_palette", slot.getInt("palette_index"))
                    val counters = slot.getJSONArray("counters")
                    edit.putInt("${prefix}_${slotIdx}_count", counters.length())
                    for (j in 0 until counters.length()) {
                        val c = counters.getJSONObject(j)
                        edit.putString("${prefix}_${slotIdx}_name_$j", c.getString("name"))
                        edit.putInt("${prefix}_${slotIdx}_color_$j", c.getInt("color_index"))
                    }
                    val palJson = slot.getJSONArray("palettes")
                    for (p in 0 until palJson.length()) {
                        val pal = palJson.getJSONArray(p)
                        for (c in 0 until pal.length()) {
                            edit.putInt("${prefix}_${slotIdx}_pal_${p}_${c}", Color.parseColor(pal.getString(c)))
                        }
                    }
                    edit.apply()
                    count++
                }
                android.widget.Toast.makeText(this, "${count}件のカウンター設定をインポートしました", android.widget.Toast.LENGTH_SHORT).show()
            } else if (isCountResult && type == "count_results") {
                val files = root.getJSONArray("files")
                var count = 0
                for (fi in 0 until files.length()) {
                    val file = files.getJSONObject(fi)
                    val fileIdx = file.getInt("file") - 1
                    if (fileIdx < 0 || fileIdx >= CNT_FILE_COUNT) continue
                    prefs.edit().putString("cnt_file_${fileIdx}_name", file.getString("file_name")).apply()
                    val slots = file.getJSONArray("slots")
                    for (si in 0 until slots.length()) {
                        val slot = slots.getJSONObject(si)
                        val slotIdx = slot.getInt("slot") - 1
                        if (slotIdx < 0 || slotIdx >= CNT_SLOTS_PER_FILE) continue
                        val prefix = "cnt_f$fileIdx"
                        val edit = prefs.edit()
                        edit.putBoolean("${prefix}_${slotIdx}_exists", true)
                        edit.putString("${prefix}_${slotIdx}_title", slot.getString("name"))
                        edit.putString("${prefix}_${slotIdx}_saved_at", slot.getString("saved_at"))
                        edit.putString("${prefix}_${slotIdx}_app_title", slot.getString("app_title"))
                        edit.putString("${prefix}_${slotIdx}_unit", slot.getString("unit"))
                        edit.putInt("${prefix}_${slotIdx}_palette", slot.getInt("palette_index"))
                        val counters = slot.getJSONArray("counters")
                        edit.putInt("${prefix}_${slotIdx}_count", counters.length())
                        for (j in 0 until counters.length()) {
                            val c = counters.getJSONObject(j)
                            edit.putString("${prefix}_${slotIdx}_name_$j", c.getString("name"))
                            edit.putInt("${prefix}_${slotIdx}_color_$j", c.getInt("color_index"))
                            edit.putInt("${prefix}_${slotIdx}_val_$j", c.getInt("count"))
                        }
                        val palJson = slot.getJSONArray("palettes")
                        for (p in 0 until palJson.length()) {
                            val pal = palJson.getJSONArray(p)
                            for (c in 0 until pal.length()) {
                                edit.putInt("${prefix}_${slotIdx}_pal_${p}_${c}", Color.parseColor(pal.getString(c)))
                            }
                        }
                        edit.apply()
                        count++
                    }
                }
                android.widget.Toast.makeText(this, "${count}件のカウント結果をインポートしました", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this, "ファイル形式が正しくありません", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "インポートに失敗しました：${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // 音声認識はカタカナや漢字で返ることがある → 両者をひらがなに正規化して比較
    private val KANJI_YOMI = listOf(
        "鮪" to "まぐろ", "烏賊" to "いか", "海老" to "えび", "蝦" to "えび",
        "鰤" to "ぶり", "玉子" to "たまご", "卵" to "たまご", "鮭" to "さけ",
        "鯛" to "たい", "鰹" to "かつお", "穴子" to "あなご", "蛸" to "たこ",
        "赤身" to "あかみ", "中トロ" to "ちゅうとろ", "大トロ" to "おおとろ",
        "軍艦" to "ぐんかん", "納豆" to "なっとう", "帆立" to "ほたて",
        "鮎" to "あゆ", "鯵" to "あじ", "鰯" to "いわし", "秋刀魚" to "さんま",
        "鰆" to "さわら", "鱈" to "たら", "河豚" to "ふぐ", "鯖" to "さば"
    )

    private fun normalizeToHiragana(text: String): String {
        var s = text
        for ((kanji, yomi) in KANJI_YOMI) s = s.replace(kanji, yomi)
        // 全角カタカナ → ひらがな (ァ=U+30A1 → ぁ=U+3041, 差=0x60)
        s = s.map { c ->
            if (c in '\u30A1'..'\u30F3') (c.toInt() - 0x60).toChar() else c
        }.joinToString("")
        return s
    }

    private fun parseVoiceCommand(text: String) {
        val normalized = normalizeToHiragana(text)
        var anyMatch = false
        var remaining = normalized
        for (i in 0 until totalCounters) {
            val normalizedName = normalizeToHiragana(names[i])
            if (remaining.contains(normalizedName)) {
                val afterName = remaining.substringAfter(normalizedName)
                val n = extractNumber(afterName)
                if (n > 0) {
                    counts[i] += n
                    countViews[i].text = counts[i].toString()
                    anyMatch = true
                }
                remaining = remaining.replace(normalizedName, "")
            }
        }
        if (!anyMatch) {
            Toast.makeText(this, "「$text」\n認識できませんでした", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "「$text」\n入力しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractNumber(text: String): Int {
        Regex("\\d+").find(text)?.value?.toIntOrNull()?.let { return it }
        val kanjiMap = mapOf("零" to 0, "一" to 1, "二" to 2, "三" to 3, "四" to 4,
            "五" to 5, "六" to 6, "七" to 7, "八" to 8, "九" to 9, "十" to 10)
        for ((k, v) in kanjiMap) { if (text.contains(k)) return v }
        val kanaMap = listOf(
            "じゅう" to 10, "きゅう" to 9, "はち" to 8, "なな" to 7, "しち" to 7,
            "ろく" to 6, "いち" to 1, "さん" to 3, "よん" to 4, "ご" to 5, "に" to 2,
            "し" to 4, "く" to 9
        )
        for ((k, v) in kanaMap) { if (text.contains(k)) return v }
        return -1
    }

    // ─── グラフView ───────────────────────────────────────────

    inner class BarChartView(
        ctx: Context,
        private val items: List<Triple<String, Int, Int>>
    ) : View(ctx) {

        private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.parseColor("#444444")
            it.textSize = dp(12).toFloat()
        }
        private val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.parseColor("#666666")
            it.textSize = dp(11).toFloat()
        }

        private val rowH  = dp(44)
        private val labW  = dp(70)
        private val rPad  = dp(36)
        private val padV  = dp(8)

        override fun onMeasure(ws: Int, hs: Int) {
            setMeasuredDimension(
                MeasureSpec.getSize(ws),
                padV * 2 + rowH * maxOf(items.size, 1))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val maxC = items.maxBy { it.second }?.second?.toFloat()?.let { if (it > 0f) it else 1f } ?: 1f
            val barMax = (width - labW - rPad).toFloat()

            items.forEachIndexed { i, (name, count, color) ->
                val top = padV + i * rowH
                val mid = top + rowH / 2
                val sn = if (name.length > 4) name.substring(0, 3) + "…" else name
                canvas.drawText(sn, dp(4).toFloat(), (mid + dp(5)).toFloat(), labelPaint)

                barPaint.color = color
                val bw = barMax * count / maxC
                if (bw > 0f)
                    canvas.drawRect(labW.toFloat(), (top + dp(8)).toFloat(),
                        labW + bw, (top + rowH - dp(8)).toFloat(), barPaint)

                canvas.drawText(count.toString(), (labW + bw + dp(4)).toFloat(),
                    (mid + dp(4)).toFloat(), numPaint)
            }
        }
    }

    inner class PieChartView(
        ctx: Context,
        private val items: List<Triple<String, Int, Int>>
    ) : View(ctx) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val legPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = Color.parseColor("#333333")
            it.textSize = dp(13).toFloat()
        }
        private val legH = dp(30)

        override fun onMeasure(ws: Int, hs: Int) {
            val w = MeasureSpec.getSize(ws)
            val chartH = minOf(w, dp(280))
            setMeasuredDimension(w, chartH + dp(8) + legH * items.size + dp(8))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val total = items.sumBy { it.second }.toFloat()
            if (total == 0f) return

            val chartH = minOf(width, dp(280)).toFloat()
            val cx = width / 2f
            val cy = chartH / 2f
            val r = minOf(cx, cy) * 0.82f
            val oval = RectF(cx - r, cy - r, cx + r, cy + r)

            var angle = -90f
            items.forEach { (_, count, color) ->
                val sweep = 360f * count / total
                paint.color = color
                canvas.drawArc(oval, angle, sweep, true, paint)
                angle += sweep
            }

            var ly = chartH.toInt() + dp(8)
            items.forEach { (name, count, color) ->
                paint.color = color
                canvas.drawRect(dp(16).toFloat(), (ly + dp(4)).toFloat(),
                    dp(32).toFloat(), (ly + dp(22)).toFloat(), paint)
                canvas.drawText("$name  $count $unit",
                    dp(40).toFloat(), (ly + dp(20)).toFloat(), legPaint)
                ly += legH
            }
        }
    }
}
