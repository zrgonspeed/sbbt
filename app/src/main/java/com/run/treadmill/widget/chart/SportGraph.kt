package com.run.treadmill.weidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.run.treadmill.R
import com.run.treadmill.widget.chart.BarGraph
import com.run.treadmill.widget.chart.SportUnitView


class SportGraph constructor(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    var view: View? = null
    var motionVar: MotionLayout? = null
    private var vBar: BarGraph? = null
    private var vUnit: SportUnitView? = null
    private var rgRraph: RadioGroup? = null
    private var tvBarVal: TextView? = null
    private var imgBarIcon: ImageView? = null
    private var rbGraphLeft: RadioButton? = null
    private var rbGraphRight: RadioButton? = null

    /** 最大值*/
    private var maxValue: Float? = null

    /** 最小值*/
    private var minValue: Float? = null

    init {
        view =
            LayoutInflater.from(context).inflate(R.layout.sport_graph, this, true)
        motionVar = view?.findViewById(R.id.motion_bar)
        view?.run {
            vBar = findViewById(R.id.v_bar)
            vUnit = findViewById(R.id.v_unit)
            rgRraph = findViewById(R.id.rg_graph)
            tvBarVal = findViewById(R.id.tv_bar_val)
            imgBarIcon = findViewById(R.id.img_bar_icon)
            rbGraphLeft = findViewById(R.id.rb_graph_left)
            rbGraphRight = findViewById(R.id.rb_graph_right)
        }
    }

    /**
     * 设置底下的切换事件
     * @param listener OnCheckedChangeListener
     */
    fun setOnGraphTypeChangeListerer(listener: RadioGroup.OnCheckedChangeListener) {
        rgRraph?.setOnCheckedChangeListener(listener)
    }

    fun setCheckBarGraph(type: Int) {
        when (type) {
            0 -> {
                rgRraph?.check(R.id.rb_graph_left)
            }
            1 -> {
                rgRraph?.check(R.id.rb_graph_right)
            }
        }
    }

    /**
     * 设置柱状图数据
     * 如有更改最大最小值范围，要先设置范围
     * @see setValueRange
     * @param datas FloatArray
     * @param inx Int
     */
    fun setArrayData(datas: FloatArray, inx: Int) {
        val tempArray = datas.copyOf(datas.size)
        maxValue?.let { max ->
            minValue?.let { min ->
                for (index in datas.indices) {
                    tempArray[index] = datas[index] / (max - min)
                }
            }
        }
        vBar?.setArrayData(tempArray, inx)
    }

    /**
     * 设置柱状图数据
     * 如有更改最大最小值范围，要先设置范围
     * @see setValueRange
     * @param datas FloatArray
     * @param inx Int
     * @param type Int  0:左边；1：右边
     */
    fun setArrayData(datas: FloatArray, inx: Int, type: Int) {
        val tempArray = datas.copyOf(datas.size)
        maxValue?.let { max ->
            minValue?.let { min ->
                for (index in datas.indices) {
                    tempArray[index] = datas[index] / (max - min)
                }
            }
        }
        vBar?.setArrayData(tempArray, inx, type)
    }

    /**
     * 设置当前段
     * @param inx Int
     */
    fun setCurrSelectInx(inx: Int) {
        vBar?.setCurrSelectInx(inx)
    }

    fun setCurrSelevtValue(type: Int, str: String) {
        tvBarVal?.text = str
        if (type == 0) {
            tvBarVal?.setTextColor(context.getColor(R.color.color_11998e))
            imgBarIcon?.setImageResource(R.drawable.img_incline_run)
        } else if (type == 1) {
            tvBarVal?.setTextColor(context.getColor(R.color.color_4b6cb7))
            imgBarIcon?.setImageResource(R.drawable.img_speed_run)
        }
    }

    /**
     * 设置数值计算的范围
     * @param min Float
     * @param max Float
     */
    fun setValueRange(min: Float, max: Float) {
        minValue = min
        maxValue = max
    }

    /**
     * 设置刻度的最大最小范围
     * @param min Float
     * @param max Float
     * @param count Int
     * @param pointCount Int
     */
    fun setUnitRange(min: Float, max: Float, count: Int, pointCount: Int) {
        vUnit?.setRange(min, max, count, pointCount)
    }

    /**
     * 设置为单个类型
     * @param str String 类型名字
     */
    fun setOnlyOneType(str: String) {
        rbGraphLeft?.text = str
        rbGraphLeft?.isEnabled = false
        rbGraphRight?.visibility = View.GONE
    }

    var currSelectType: Int? = null
}