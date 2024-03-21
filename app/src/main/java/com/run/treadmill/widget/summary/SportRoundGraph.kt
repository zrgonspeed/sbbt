package com.run.treadmill.widget.summary

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.run.treadmill.R

class SportRoundGraph : ConstraintLayout {
    var view: View? = null
    private var roundProgress: RoundGraphView? = null
    private var tvValue: TextView? = null
    private var tvTip: TextView? = null
    private var tvUnit: TextView? = null

    companion object {
        @JvmStatic
        @BindingAdapter("txtRpgUnit")
        fun setRpgUnit(view: SportRoundGraph, resId: Int) {
            view.setUnit(view.context.getString(resId))
        }
    }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        view = LayoutInflater.from(context)
            .inflate(R.layout.view_sport_round_progress_graph, this, true)
        view?.run {
            roundProgress = findViewById(R.id.round_progress)
            tvValue = findViewById(R.id.tv_value)
            tvTip = findViewById(R.id.tv_tip)
            tvUnit = findViewById(R.id.tv_unit)
        }

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.SportRoundProgressGraph)
        roundProgress?.setRoundBarGraphAttributes(
            typeArray.getColor(
                R.styleable.SportRoundProgressGraph_sport_rpg_start_color,
                Color.MAGENTA
            ),
            typeArray.getColor(
                R.styleable.SportRoundProgressGraph_sport_rpg_end_color,
                Color.MAGENTA
            ),
            typeArray.getFloat(R.styleable.SportRoundProgressGraph_sport_rpg_start_angle, -90f),
            typeArray.getFloat(R.styleable.SportRoundProgressGraph_sport_rpg_sweep_angle, 360f)
        )

        tvValue?.text = typeArray.getText(R.styleable.SportRoundProgressGraph_sport_rpg_value)
        tvTip?.text = typeArray.getText(R.styleable.SportRoundProgressGraph_sport_rpg_tip)
        tvUnit?.text = typeArray.getText(R.styleable.SportRoundProgressGraph_sport_rpg_unit)
        typeArray.recycle()
    }

    /**
     * 设置进度
     * @param progress Int
     * @param duration Long
     */
    fun setRoundProgress(progress: Int, duration: Long) {
        roundProgress?.setProgressAtAnim(progress, duration)
    }

    fun setValue(str: String) {
        tvValue?.text = str
        postInvalidate()
    }

    /**
     * 设置属性
     * @param tip String?
     * @param unit String?
     */
    fun setInfo(tip: String?, unit: String?) {
        tip?.let {
            tvTip?.text = tip
        }
        tvTip?.visibility = if (tip == null) View.GONE else View.VISIBLE

        unit?.let {
            tvUnit?.text = unit
        }
        postInvalidate()
    }

    /**
     * 设置单位
     * @param unit
     */
    fun setUnit(unit: String?) {
        unit?.let {
            tvUnit?.text = unit
        }
        postInvalidate()
    }
}