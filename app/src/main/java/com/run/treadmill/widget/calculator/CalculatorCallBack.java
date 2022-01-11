package com.run.treadmill.widget.calculator;

import com.run.treadmill.common.CTConstant;

public interface CalculatorCallBack {
    /**
     * @param type  输入数据类型 比如年龄,时间等,与BaseCalculator.setEditType(int type)一致
     * @param value 与极限值比较后得出的结果,根据需求可以手动强制转换
     */
    void enterCallBack(@CTConstant.EditType int type, String value);

    void onCalculatorDismiss();
}
