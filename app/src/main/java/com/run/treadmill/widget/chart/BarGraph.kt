package com.run.treadmill.widget.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.run.treadmill.R
import com.run.treadmill.common.InitParam
import com.run.treadmill.reboot.MyApplication

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2021/08/10
 */
class BarGraph : View {
    private var barPaint = Paint()
    private var barRectF = RectF()

    /** 条形图的数量*/
    private var barNum = InitParam.TOTAL_RUN_STAGE_NUM

    /** 每条柱子的百分比*/
    private var barPointArray = FloatArray(barNum)

    /** 当前位置*/
    private var currInx = 10
        set(value) {
            field = value
            postInvalidate()
        }

    private var barWidth = 30f
    private var barInterval = 5f

    /*** 已完成的颜色（渐变色的开始）*/
    private var hasCompletedColorStart = Color.WHITE

    /*** 已完成的颜色（渐变色的结束）*/
    private var hasCompletedColorEnd = Color.WHITE

    /** 当前的*/
    private var currCompletedColorStart = Color.GREEN

    /** 当前的*/
    private var currCompletedColorEnd = Color.GREEN

    /** 未开始的*/
    private var unCompletedColorStart = Color.MAGENTA

    /** 未开始的*/
    private var unCompletedColorEnd = Color.MAGENTA

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.let {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.BarGraph)
            hasCompletedColorStart =
                typeArray.getColor(R.styleable.BarGraph_bar_hasCompletedColorStart, Color.WHITE)
            currCompletedColorStart =
                typeArray.getColor(R.styleable.BarGraph_bar_currCompletedColorStart, Color.GREEN)
            unCompletedColorStart =
                typeArray.getColor(R.styleable.BarGraph_bar_unCompletedColorStart, Color.MAGENTA)
            hasCompletedColorEnd = typeArray.getColor(
                R.styleable.BarGraph_bar_hasCompletedColorEnd,
                hasCompletedColorStart
            )
            currCompletedColorEnd = typeArray.getColor(
                R.styleable.BarGraph_bar_currCompletedColorEnd,
                currCompletedColorStart
            )
            unCompletedColorEnd = typeArray.getColor(
                R.styleable.BarGraph_bar_unCompletedColorEnd,
                unCompletedColorStart
            )

            typeArray.recycle()
        }
        barPaint.isAntiAlias = true
        barPaint.style = Paint.Style.FILL

        for (index in barPointArray.indices) {
            barPointArray[index] = index * 0.01f
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        barWidth = (width - ((barNum - 1) * barInterval)) / barNum
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for ((index, value) in barPointArray.withIndex()) {
            barRectF.left = index * (barInterval + barWidth)
            barRectF.right = barWidth * (index + 1) + barInterval * index
            barRectF.bottom = height.toFloat()
            barRectF.top = if (value == 0f) height - 1f else height * (1 - value)

            when {
                index < currInx -> {
                    if (hasCompletedColorStart == hasCompletedColorEnd) {
                        barPaint.shader = null
                        barPaint.color = hasCompletedColorStart
                    } else {
                        //着色器,只能创建，无法优化了
                        val shader = LinearGradient(
                            barRectF.left, 0f, barRectF.right, 0f,
                            hasCompletedColorStart, hasCompletedColorEnd, Shader.TileMode.CLAMP
                        )
                        barPaint.shader = shader
                    }
                }
                index == currInx -> {
                    if (currCompletedColorStart == currCompletedColorEnd) {
                        barPaint.shader = null
                        barPaint.color = currCompletedColorStart
                    } else {
                        //着色器,只能创建，无法优化了
                        val shader = LinearGradient(
                            barRectF.left, 0f, barRectF.right, 0f,
                            currCompletedColorStart, currCompletedColorEnd, Shader.TileMode.CLAMP
                        )
                        barPaint.shader = shader
                    }
                }
                else -> {
                    if (unCompletedColorStart == unCompletedColorEnd) {
                        barPaint.shader = null
                        barPaint.color = unCompletedColorStart
                    } else {
                        //着色器,只能创建，无法优化了
                        val shader = LinearGradient(
                            barRectF.left, 0f, barRectF.right, 0f,
                            unCompletedColorStart, unCompletedColorEnd, Shader.TileMode.CLAMP
                        )
                        barPaint.shader = shader
                    }
                }
            }
            canvas?.drawRoundRect(barRectF, 5f, 5f, barPaint)
        }
    }

    /**
     * 设置当前段
     * @param inx Int
     */
    fun setCurrSelectInx(inx: Int) {
        currInx = inx
        postInvalidate()
    }

    /**
     * 设置数据
     * @param datas FloatArray
     * @param inx Int
     */
    fun setArrayData(datas: FloatArray, inx: Int) {
        System.arraycopy(datas, 0, barPointArray, 0, barNum)
        currInx = inx
        postInvalidate()
    }

    /**
     * 设置数据
     * @param datas FloatArray
     * @param inx Int
     * @param type Int 0:左边；1：右边
     */
    fun setArrayData(datas: FloatArray, inx: Int, type: Int) {
        System.arraycopy(datas, 0, barPointArray, 0, barNum)
        currInx = inx

        changeColor(type)
    }

    /**
     * 目前就两个固定颜色
     * @param type Int 0:左边；1：右边
     */
    private fun changeColor(type: Int) {
        if (type == 0) {
            currCompletedColorStart = ContextCompat.getColor(context, R.color.color_38ef7d)
            currCompletedColorEnd = ContextCompat.getColor(context, R.color.color_11998e)
            unCompletedColorStart = ContextCompat.getColor(context, R.color.color_44a08d)
            unCompletedColorEnd = ContextCompat.getColor(context, R.color.color_093637)
        } else if (type == 1) {
            currCompletedColorStart = ContextCompat.getColor(context, R.color.color_00c6ff)
            currCompletedColorEnd = ContextCompat.getColor(context, R.color.color_0072ff)
            unCompletedColorStart = ContextCompat.getColor(context, R.color.color_4b6cb7)
            unCompletedColorEnd = ContextCompat.getColor(context, R.color.color_182848)
        }

        postInvalidate()
    }

    fun setMyColorLeft() {
        hasCompletedColorStart = MyApplication.getContext()!!.getColor(R.color.summary_1)
        currCompletedColorStart = MyApplication.getContext()!!.getColor(R.color.summary_1)
        unCompletedColorStart = MyApplication.getContext()!!.getColor(R.color.summary_1)

        hasCompletedColorEnd = MyApplication.getContext()!!.getColor(R.color.summary_1_1)
        currCompletedColorEnd = MyApplication.getContext()!!.getColor(R.color.summary_1_1)
        unCompletedColorEnd = MyApplication.getContext()!!.getColor(R.color.summary_1_1)
    }

    fun setMyColorRight() {
        hasCompletedColorStart = MyApplication.getContext()!!.getColor(R.color.summary_2)
        currCompletedColorStart = MyApplication.getContext()!!.getColor(R.color.summary_2)
        unCompletedColorStart = MyApplication.getContext()!!.getColor(R.color.summary_2)

        hasCompletedColorEnd = MyApplication.getContext()!!.getColor(R.color.summary_2_1)
        currCompletedColorEnd = MyApplication.getContext()!!.getColor(R.color.summary_2_1)
        unCompletedColorEnd = MyApplication.getContext()!!.getColor(R.color.summary_2_1)
    }
}