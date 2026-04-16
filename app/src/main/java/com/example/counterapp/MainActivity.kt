package com.example.counterapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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

    private val PASTEL_COLORS = intArrayOf(
        Color.parseColor("#C8B89A"),
        Color.parseColor("#D4A882"),
        Color.parseColor("#A4B898"),
        Color.parseColor("#B8A898"),
        Color.parseColor("#C4906C"),
        Color.parseColor("#96A8A8"),
        Color.parseColor("#B89494"),
        Color.parseColor("#A8A87C")
    )

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        prefs = getSharedPreferences("sushi_prefs", Context.MODE_PRIVATE)
        totalCounters = prefs.getInt("counter_count", 6)

        for (i in 0 until totalCounters) {
            counts.add(savedInstanceState?.getInt("count_$i", 0) ?: 0)
            val def = if (i < DEFAULT_NAMES.size) DEFAULT_NAMES[i] else "ネタ${i + 1}"
            names.add(prefs.getString("name_$i", def) ?: def)
            colorIndices.add(prefs.getInt("color_$i", 0))
        }

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

        val sbId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val topInset = if (sbId > 0) resources.getDimensionPixelSize(sbId) else dp(28)
        root.addView(View(this), LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, topInset))

        root.addView(TextView(this).apply {
            text = "西川さんお寿司カウンター"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1976D2"))
            setGravity(Gravity.CENTER)
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
            it.topMargin = dp(6)
        })

        scrollView.addView(inner, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        root.addView(scrollView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // 下部ボタンバー（全リセット ＋ メニュー）
        val bottomBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val resetBtn = Button(this).apply {
            text = "全リセット"
            setTextColor(Color.parseColor("#F44336"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#F44336"))
            }
            setOnClickListener { showAllResetDialog() }
        }
        val menuBtn = Button(this).apply {
            text = "メニュー ▲"
            setTextColor(Color.parseColor("#1976D2"))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(2), Color.parseColor("#1976D2"))
            }
        }
        menuBtn.setOnClickListener { showMenuPopup(menuBtn) }

        bottomBar.addView(resetBtn, LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        bottomBar.addView(menuBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
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
            setBackgroundColor(PASTEL_COLORS[colorIndices[index]])
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
            setTextColor(Color.parseColor("#90A4AE"))
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
            counts[index]++; countView.text = counts[index].toString()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h))
        btnLayout.addView(btn("－", "#546E7A", "#546E7A") {
            if (counts[index] > 0) { counts[index]--; countView.text = counts[index].toString() }
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
                    if (from >= 0 && from != index) swapCounters(from, index)
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

    private fun swapCounters(a: Int, b: Int) {
        val tc = counts[a];        counts[a] = counts[b];        counts[b] = tc
        val tn = names[a];         names[a] = names[b];          names[b] = tn
        val tci = colorIndices[a]; colorIndices[a] = colorIndices[b]; colorIndices[b] = tci
        prefs.edit()
            .putString("name_$a", names[a]).putString("name_$b", names[b])
            .putInt("color_$a", colorIndices[a]).putInt("color_$b", colorIndices[b])
            .apply()
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

    private fun showAllResetDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("全リセット")
            .setMessage("デフォルト状態に戻します。\n追加したカウンターは削除され、カウントも0になります。")
            .setPositiveButton("リセット") { _, _ ->
                val editor = prefs.edit()
                for (i in 0 until totalCounters) {
                    editor.remove("name_$i").remove("color_$i")
                }
                editor.putInt("counter_count", 6).apply()
                counts.clear(); names.clear(); colorIndices.clear()
                totalCounters = 6
                for (i in 0 until 6) {
                    counts.add(0); names.add(DEFAULT_NAMES[i]); colorIndices.add(0)
                }
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
        val items = arrayOf("棒グラフ（多い順・0含む）", "円グラフ（多い順・0除外）", "マニュアル")
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("メニュー")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showBarChartDialog()
                    1 -> showPieChartDialog()
                    2 -> showManualDialog()
                }
            }
            .setNegativeButton("キャンセル", null).show()
    }

    private fun showBarChartDialog() {
        val items = (0 until totalCounters)
            .map { Triple(names[it], counts[it], PASTEL_COLORS[colorIndices[it]]) }
            .sortedByDescending { it.second }
        val sv = ScrollView(this).apply { setPadding(dp(4), dp(4), dp(4), dp(4)) }
        sv.addView(BarChartView(this, items), ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("棒グラフ（多い順・0含む）")
            .setView(sv)
            .setNegativeButton("閉じる", null).show()
    }

    private fun showPieChartDialog() {
        val items = (0 until totalCounters)
            .map { Triple(names[it], counts[it], PASTEL_COLORS[colorIndices[it]]) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
        val sv = ScrollView(this).apply { setPadding(dp(4), dp(4), dp(4), dp(4)) }
        if (items.isEmpty()) {
            sv.addView(TextView(this).apply {
                text = "カウントがすべて0のため表示できません"
                setPadding(dp(16), dp(16), dp(16), dp(16))
            }, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        } else {
            sv.addView(PieChartView(this, items), ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("円グラフ（多い順・0除外）")
            .setView(sv)
            .setNegativeButton("閉じる", null).show()
    }

    private fun showManualDialog() {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("マニュアル")
            .setMessage(
                "■ 名前タップ\n名前の変更ダイアログが開きます\n\n" +
                "■ 名前を長押し\n背景色の変更ダイアログが開きます\n\n" +
                "■ 数字を長押し\nカードをドラッグして並べ替えができます\n\n" +
                "■ カード右上 ☒\nカードを削除します（確認ダイアログあり）\n\n" +
                "■ 全リセットボタン\nデフォルト6種類に戻し、カウントをすべて0にします"
            )
            .setNegativeButton("閉じる", null).show()
    }

    // ─── 色選択ダイアログ ─────────────────────────────────────

    private fun showColorDialog(index: Int) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
        val dialogHolder = arrayOfNulls<AlertDialog>(1)

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
                        topSections[index].setBackgroundColor(PASTEL_COLORS[ci])
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
                counts.add(0); names.add(name); colorIndices.add(0)
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
                canvas.drawText("$name  $count 皿",
                    dp(40).toFloat(), (ly + dp(20)).toFloat(), legPaint)
                ly += legH
            }
        }
    }
}
