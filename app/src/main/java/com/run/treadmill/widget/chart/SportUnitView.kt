package com.run.treadmill.widget.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.run.treadmill.R
import com.run.treadmill.util.UnitUtil

class SportUnitView : View {
    private var txtPaint = Paint()
    private var txtRect = Rect()

    /** 文字的方向，左右*/
    private var txtGravity = 1

    private var mColor = Color.BLACK

    /** 刻度最小值*/
    var minValue = 0f

    /** 刻度最大值*/
    var maxValue = 30f

    /** 刻度值的个数*/
    private var valCount = 6

    /** 刻度值的小数个数*/
    private var mPointCount = 0

    /** 刻度值大小*/
    private var txtSize = 30f

    /** 刻度直线的是粗细*/
    private var lineStrokeWidth = 5f

    /** 刻度线的粗细*/
    private var scaleStrokeWidth = 5f

    /** 刻度长度*/
    private var scaleWidth = 20f

    /** 上下刻度到控件的边距*/
    private var scanMarginY = 10f

    /** 刻度值的间隔*/
    private var interval = 0f

    /** 刻度数值*/
    private var txtArray: Array<String?>? = null

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
        attrs?.let {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleUnitView)
            txtGravity = typeArray.getInteger(R.styleable.ScaleUnitView_suv_txt_gravity, 1)
            mColor = typeArray.getColor(R.styleable.ScaleUnitView_suv_color, Color.WHITE)
            minValue = typeArray.getFloat(R.styleable.ScaleUnitView_suv_min_value, 0f)
            maxValue = typeArray.getFloat(R.styleable.ScaleUnitView_suv_max_value, 30f)
            valCount = typeArray.getInteger(R.styleable.ScaleUnitView_suv_val_count, 6)
            mPointCount = typeArray.getInteger(R.styleable.ScaleUnitView_suv_val_point_count, 0)
            lineStrokeWidth =
                typeArray.getFloat(R.styleable.ScaleUnitView_suv_line_stroke_width, 5f)
            scaleStrokeWidth =
                typeArray.getFloat(R.styleable.ScaleUnitView_suv_scale_stroke_width, 5f)
            scaleWidth = typeArray.getFloat(R.styleable.ScaleUnitView_suv_scale_width, 20f)
            scanMarginY = typeArray.getFloat(R.styleable.ScaleUnitView_suv_margin_y, 10f)
            txtSize = typeArray.getFloat(R.styleable.ScaleUnitView_suv_txt_size, 30f)
            typeArray.recycle()
        }


        txtPaint.color = mColor
        txtPaint.isAntiAlias = true
        txtPaint.style = Paint.Style.FILL
        txtPaint.textSize = txtSize
        if (txtGravity == 1) {
            txtPaint.textAlign = Paint.Align.RIGHT
        } else if (txtGravity == 2) {
            txtPaint.textAlign = Paint.Align.LEFT
        }
        txtPaint.strokeWidth = scaleStrokeWidth
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        interval = (maxValue - minValue) / (valCount - 1)

        txtArray = arrayOfNulls(valCount)
        for (index in 0 until valCount) {
//            txtArray!![index] = ((maxValue - minValue) / (valCount - 1) * (valCount - index) + minValue).toString()
            txtArray!![index] =
                UnitUtil.getPoint(maxValue - (minValue + index * interval), mPointCount)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        txtPaint.getTextBounds(txtArray!![0], 0, txtArray!![0]?.length!!, txtRect)

        var beelineStartX = 0f
        var txtX = 0f
        var scaleStartX = 0f
        var scaleStopX = 0f
        when (txtGravity) {
            1 -> {
                beelineStartX = width - scaleStrokeWidth
                txtX = txtRect.width().toFloat() + 2
                scaleStartX = width - scaleStrokeWidth
                scaleStopX = width - scaleStrokeWidth - scaleWidth
            }
            2 -> {
                txtX = width - txtRect.width().toFloat()
                scaleStartX = scaleStrokeWidth
                scaleStopX = scaleStrokeWidth + scaleWidth
            }
            else -> {
            }
        }
        //画直线
        if (lineStrokeWidth > 0) {
            txtPaint.strokeWidth = lineStrokeWidth
            canvas?.drawLine(
                beelineStartX,
                scanMarginY,
                beelineStartX,
                height - scanMarginY,
                txtPaint
            )
        }
        txtPaint.strokeWidth = scaleStrokeWidth
        //刻度间隔
        val scaleIntervalHeight = (height - 2f * scanMarginY) / (valCount - 1).toFloat()
        for (c in 0 until valCount) {
            txtArray?.get(c)?.let {
                canvas?.drawText(
                    it,
                    txtX,
                    c * scaleIntervalHeight + scanMarginY + txtRect.height() / 2f,
                    txtPaint
                )
            }
            canvas?.drawLine(
                scaleStartX, c * scaleIntervalHeight + scanMarginY,
                scaleStopX, c * scaleIntervalHeight + scanMarginY, txtPaint
            )
        }
    }

    fun setTxtArray(txts: Array<String?>) {
        txtArray = txts
        valCount = txts.size
        postInvalidate()
    }

    /**
     * 设置范围，计算刻度
     * @param min Float
     * @param max Float
     * @param count Int  刻度个数
     * @param pointCount Int 刻度值的小数个数
     */
    fun setRange(min: Float, max: Float, count: Int, pointCount: Int) {
        if (minValue == min && maxValue == max && valCount == count && mPointCount == pointCount) {
            return
        }
        minValue = min
        maxValue = max
        valCount = count
        mPointCount = pointCount

        interval = (maxValue - minValue) / (valCount - 1)

        txtArray = arrayOfNulls(valCount)
        for (index in 0 until valCount) {
//            txtArray!![index] = ((maxValue - minValue) / (valCount - 1) * (valCount - index) + minValue).toString()
            txtArray!![index] =
                UnitUtil.getPoint(maxValue - (minValue + index * interval), mPointCount)
        }

        postInvalidate()
    }
}