package com.run.treadmill.widget.summary

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.Keep
import com.run.treadmill.R

class RoundGraphView : View {
    private val pbPaint = Paint()
    private val bgPaint = Paint()
    private val rectF = RectF()

    /** 圆心坐标*/
    private var cX = 0f
    private var cY = 0f

    private var radius = 200f

    /** 范围0~100 */
    private var progress = 100
        @Keep set(value) {
            field = value
            postInvalidate()
        }
    private var pbWidth = 35f

    /** 开始角度*/
    private var startAngle = -90f

    /** 扫过的角度（顺时针）*/
    private var sweepAngle = 360f

    /** 是否需要背景圈*/
    private var needBg = true

    private var startColor = Color.MAGENTA
    private var endColor = Color.RED
    private var bgColor = Color.GRAY

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
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBarGraph)
//            radius = typeArray.getFloat(R.styleable.RoundProgressBarGraph_rpb_radius, 50f)
//            pbWidth = typeArray.getFloat(R.styleable.RoundProgressBarGraph_rpb_width, 20f)
            radius = typeArray.getDimension(R.styleable.RoundProgressBarGraph_rpb_radius, 50f)
            pbWidth = typeArray.getDimension(R.styleable.RoundProgressBarGraph_rpb_width, 20f)
            startColor =
                typeArray.getColor(R.styleable.RoundProgressBarGraph_rpb_start_color, Color.MAGENTA)
            endColor =
                typeArray.getColor(R.styleable.RoundProgressBarGraph_rpb_end_color, startColor)
            needBg = typeArray.getBoolean(R.styleable.RoundProgressBarGraph_rpb_background, true)
            if (needBg) {
                bgColor = typeArray.getColor(
                    R.styleable.RoundProgressBarGraph_rpb_background_color,
                    Color.GRAY
                )
            }
            startAngle = typeArray.getFloat(R.styleable.RoundProgressBarGraph_cpb_start_angle, -90f)
            sweepAngle = typeArray.getFloat(R.styleable.RoundProgressBarGraph_cpb_sweep_angle, 360f)
            typeArray.recycle()
        }

        //抗锯齿
        pbPaint.isAntiAlias = true
        pbPaint.style = Paint.Style.STROKE
        pbPaint.strokeCap = Paint.Cap.ROUND
        pbPaint.strokeWidth = pbWidth

        bgPaint.isAntiAlias = true
        bgPaint.style = Paint.Style.STROKE
        bgPaint.strokeCap = Paint.Cap.ROUND
        bgPaint.strokeWidth = pbWidth
        bgPaint.color = bgColor
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        cX = width / 2f
        cY = height / 2f
        val colors = intArrayOf(startColor, endColor, startColor)
        val positions = floatArrayOf(0f, 0.5f, 1f)

//        pbPaint.shader = SweepGradient(cX, cY, startColor, endColor)
        pbPaint.shader = SweepGradient(cX, cY, colors, positions)

        rectF.left = cX - radius
        rectF.top = cY - radius
        rectF.right = cX + radius
        rectF.bottom = cY + radius
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (needBg) {
            canvas?.drawArc(rectF, startAngle, sweepAngle, false, bgPaint)
        }
        canvas?.drawArc(rectF, startAngle, sweepAngle * progress / 100f, false, pbPaint)
    }

    /**
     * 设置基本属性
     * @param startColor Int
     * @param endColor Int
     * @param startAngle Float
     * @param sweepAngle Float
     */
    fun setRoundBarGraphAttributes(
        startColor: Int,
        endColor: Int,
        startAngle: Float,
        sweepAngle: Float
    ) {
        this.startColor = startColor
        this.endColor = endColor
        this.startAngle = startAngle
        this.sweepAngle = sweepAngle
    }

    /**
     * 设置带动画的进度
     * @param progree Int 0~100
     * @param duration Long
     */
    fun setProgressAtAnim(progree: Int, duration: Long) {
        val oAnim = ObjectAnimator.ofInt(this, "progress", 0, progree)
        oAnim.duration = duration
        oAnim.interpolator = OvershootInterpolator()
        oAnim.start()
    }
}