package com.run.treadmill.common;

/**
 * @Description 公制的数值表
 * @Author GaleLiu
 * @Time 2019/07/02
 */
public final class RunModeTable {
    private RunModeTable() {

    }

    // 0是speed， 1是incline, 单数incline, 双数speed
    public static float PModeTable[][] = {
            {2f, 2f, 4f, 4f, 6f, 6f, 8f, 8f, 10f, 10f, 12f, 12f, 14f, 14f, 16f, 16f, 16f, 14f, 14f, 12f, 12f, 10f, 10f, 8f, 8f, 6f, 4f, 4f, 2f, 2f},
            {1f, 1f, 2f, 2f, 3f, 3f, 3f, 1f, 2f, 2f, 3f, 3f, 2f, 2f, 2f, 2f, 3f, 3f, 5f, 5f, 3f, 3f, 4f, 2f, 3f, 4f, 2f, 2f, 3f, 3f},
            {2f, 2f, 4f, 4f, 4f, 4f, 6f, 6f, 6f, 6f, 6f, 6f, 8f, 8f, 10f, 10f, 10f, 8f, 8f, 8f, 8f, 6f, 6f, 6f, 6f, 4f, 4f, 4f, 2f, 2f},
            {1f, 1f, 2f, 2f, 3f, 3f, 3f, 2f, 2f, 3f, 4f, 4f, 2f, 2f, 2f, 2f, 3f, 3f, 5f, 5f, 4f, 4f, 4f, 3f, 4f, 4f, 3f, 3f, 1f, 1f},
            {2f, 2f, 4f, 4f, 6f, 6f, 8f, 8f, 10f, 10f, 6f, 4f, 6f, 8f, 10f, 10f, 8f, 6f, 6f, 4f, 4f, 8f, 8f, 6f, 6f, 6f, 4f, 4f, 2f, 2f},
            {1f, 1f, 2f, 2f, 2f, 2f, 3f, 1f, 2f, 2f, 2f, 2f, 2f, 2f, 1f, 1f, 3f, 3f, 5f, 5f, 3f, 3f, 4f, 2f, 3f, 4f, 2f, 2f, 3f, 3f},
            {2f, 2f, 4f, 4f, 6f, 6f, 8f, 8f, 8f, 8f, 10f, 10f, 10f, 12f, 12f, 12f, 12f, 10f, 10f, 8f, 8f, 8f, 8f, 8f, 8f, 6f, 4f, 4f, 2f, 2f},
            {2f, 2f, 2f, 2f, 3f, 3f, 3f, 2f, 2f, 4f, 6f, 6f, 2f, 2f, 2f, 2f, 3f, 3f, 4f, 4f, 6f, 6f, 2f, 3f, 7f, 9f, 5f, 5f, 2f, 2f},
            {2f, 2f, 4f, 4f, 8f, 8f, 8f, 8f, 12f, 12f, 6f, 12f, 6f, 12f, 12f, 12f, 6f, 6f, 6f, 12f, 12f, 12f, 12f, 6f, 6f, 12f, 6f, 6f, 6f, 6f},
            {1f, 1f, 2f, 2f, 4f, 4f, 3f, 2f, 2f, 4f, 5f, 5f, 2f, 2f, 1f, 1f, 3f, 3f, 5f, 5f, 6f, 6f, 7f, 8f, 9f, 10f, 10f, 8f, 2f, 2f},
            {4f, 4f, 6f, 6f, 10f, 10f, 10f, 10f, 8f, 8f, 8f, 8f, 8f, 10f, 10f, 10f, 6f, 4f, 4f, 4f, 4f, 6f, 6f, 10f, 10f, 10f, 6f, 6f, 4f, 4f},
            {2f, 2f, 2f, 2f, 6f, 6f, 2f, 3f, 4f, 2f, 2f, 2f, 2f, 2f, 1f, 1f, 4f, 4f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 3f, 3f, 2f, 2f},
            {4f, 4f, 4f, 4f, 6f, 6f, 6f, 6f, 8f, 8f, 8f, 8f, 10f, 10f, 8f, 8f, 8f, 8f, 8f, 6f, 6f, 6f, 6f, 6f, 6f, 4f, 4f, 4f, 4f, 4f},
            {4f, 4f, 5f, 5f, 6f, 6f, 6f, 9f, 9f, 10f, 7f, 7f, 6f, 6f, 3f, 3f, 4f, 4f, 2f, 2f, 4f, 4f, 6f, 8f, 7f, 8f, 6f, 6f, 2f, 2f},
            {2f, 2f, 4f, 4f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 8f, 8f, 8f, 10f, 10f, 10f, 12f, 12f, 14f, 14f, 12f, 12f, 10f, 10f, 8f, 6f, 6f, 4f, 4f},
            {3f, 3f, 5f, 5f, 4f, 4f, 4f, 3f, 4f, 4f, 3f, 3f, 3f, 3f, 2f, 2f, 4f, 4f, 2f, 2f, 4f, 4f, 6f, 8f, 7f, 8f, 6f, 6f, 2f, 2f},
            {2f, 2f, 4f, 4f, 5f, 5f, 5f, 6f, 5f, 6f, 3f, 3f, 3f, 3f, 2f, 2f, 3f, 3f, 4f, 4f, 4f, 4f, 3f, 5f, 5f, 6f, 3f, 3f, 3f, 3f},
            {3f, 3f, 5f, 5f, 3f, 3f, 4f, 2f, 3f, 4f, 2f, 2f, 3f, 3f, 2f, 2f, 3f, 3f, 5f, 5f, 4f, 4f, 4f, 3f, 4f, 3f, 2f, 2f, 2f, 2f},
            {2f, 2f, 3f, 3f, 5f, 5f, 3f, 3f, 5f, 3f, 6f, 6f, 3f, 3f, 3f, 3f, 4f, 4f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 3f, 3f, 2f, 2f},
            {4f, 4f, 4f, 4f, 3f, 3f, 6f, 7f, 8f, 8f, 6f, 6f, 3f, 3f, 3f, 3f, 3f, 3f, 2f, 2f, 5f, 5f, 7f, 3f, 5f, 6f, 7f, 7f, 2f, 2f},
            {3f, 3f, 5f, 5f, 8f, 8f, 8f, 9f, 5f, 7f, 6f, 6f, 3f, 3f, 2f, 2f, 3f, 3f, 4f, 4f, 6f, 6f, 2f, 3f, 7f, 9f, 5f, 5f, 2f, 2f},
            {4f, 4f, 5f, 5f, 6f, 6f, 6f, 9f, 9f, 10f, 8f, 8f, 6f, 6f, 3f, 3f, 3f, 3f, 4f, 4f, 4f, 4f, 3f, 5f, 5f, 6f, 3f, 3f, 3f, 3f},
            {2f, 2f, 5f, 5f, 5f, 5f, 4f, 4f, 6f, 4f, 2f, 2f, 3f, 3f, 4f, 4f, 3f, 3f, 4f, 4f, 3f, 3f, 4f, 3f, 5f, 4f, 2f, 2f, 1f, 1f},
            {3f, 3f, 5f, 5f, 6f, 6f, 7f, 8f, 9f, 10f, 10f, 8f, 6f, 6f, 3f, 3f, 5f, 5f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 5f, 5f, 2f, 2f},
            {4f, 4f, 2f, 2f, 6f, 6f, 8f, 6f, 3f, 2f, 6f, 6f, 2f, 2f, 2f, 2f, 3f, 3f, 10f, 10f, 7f, 7f, 10f, 7f, 10f, 7f, 7f, 7f, 2f, 2f},
            {3f, 3f, 4f, 4f, 6f, 6f, 2f, 3f, 7f, 9f, 5f, 5f, 2f, 2f, 2f, 2f, 1f, 1f, 2f, 2f, 4f, 4f, 5f, 2f, 2f, 4f, 6f, 6f, 3f, 3f},
            {3f, 3f, 4f, 4f, 4f, 4f, 3f, 5f, 5f, 6f, 3f, 3f, 3f, 3f, 2f, 2f, 5f, 5f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 5f, 5f, 2f, 2f},
            {3f, 3f, 5f, 5f, 4f, 4f, 4f, 3f, 4f, 3f, 2f, 2f, 2f, 2f, 4f, 4f, 2f, 2f, 5f, 5f, 4f, 4f, 2f, 4f, 2f, 4f, 2f, 2f, 4f, 4f},
            {4f, 4f, 2f, 2f, 4f, 4f, 6f, 8f, 7f, 8f, 6f, 6f, 2f, 2f, 3f, 3f, 2f, 2f, 5f, 5f, 4f, 4f, 2f, 4f, 2f, 4f, 2f, 2f, 4f, 4f},
            {4f, 4f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 3f, 3f, 2f, 2f, 5f, 5f, 1f, 1f, 2f, 2f, 3f, 3f, 3f, 2f, 2f, 3f, 4f, 4f, 2f, 2f},
            {3f, 3f, 2f, 2f, 5f, 5f, 7f, 3f, 5f, 6f, 7f, 7f, 2f, 2f, 1f, 1f, 2f, 2f, 5f, 5f, 4f, 4f, 5f, 4f, 5f, 4f, 6f, 6f, 3f, 3f},
            {3f, 5f, 9f, 10f, 9f, 9f, 8f, 8f, 4f, 3f, 3f, 3f, 2f, 2f, 4f, 4f, 1f, 1f, 2f, 2f, 2f, 2f, 3f, 1f, 2f, 2f, 1f, 1f, 2f, 2f},
            {2f, 4f, 6f, 8f, 8f, 10f, 10f, 8f, 10f, 10f, 5f, 5f, 9f, 9f, 8f, 8f, 5f, 5f, 4f, 4f, 3f, 4f, 3f, 4f, 5f, 6f, 3f, 3f, 3f, 3f},
            {1f, 1f, 4f, 4f, 6f, 6f, 6f, 1f, 4f, 4f, 6f, 6f, 4f, 4f, 4f, 4f, 3f, 3f, 5f, 5f, 4f, 4f, 2f, 2f, 3f, 4f, 2f, 2f, 3f, 3f},
            {3f, 5f, 10f, 10f, 7f, 7f, 8f, 10f, 8f, 10f, 10f, 10f, 7f, 7f, 5f, 5f, 7f, 7f, 10f, 10f, 9f, 9f, 8f, 8f, 4f, 3f, 3f, 3f, 2f, 2f},
            {1f, 1f, 4f, 4f, 4f, 4f, 6f, 1f, 4f, 4f, 6f, 6f, 8f, 8f, 4f, 4f, 5f, 5f, 10f, 10f, 8f, 8f, 10f, 8f, 10f, 10f, 5f, 5f, 2f, 1f},
            {3f, 3f, 7f, 7f, 5f, 5f, 7f, 6f, 10f, 7f, 3f, 3f, 10f, 10f, 5f, 5f, 4f, 4f, 6f, 6f, 7f, 7f, 5f, 8f, 6f, 7f, 6f, 6f, 3f, 1f},
            {1f, 1f, 2f, 2f, 4f, 4f, 5f, 1f, 2f, 2f, 4f, 4f, 3f, 3f, 3f, 3f, 1f, 1f, 3f, 3f, 4f, 4f, 3f, 2f, 2f, 4f, 5f, 5f, 2f, 2f},
            {3f, 3f, 10f, 10f, 7f, 7f, 10f, 7f, 10f, 7f, 7f, 7f, 7f, 7f, 3f, 3f, 4f, 4f, 6f, 6f, 7f, 7f, 5f, 8f, 6f, 7f, 6f, 6f, 3f, 3f},
            {1f, 1f, 2f, 2f, 4f, 4f, 5f, 2f, 2f, 4f, 6f, 6f, 3f, 3f, 2f, 2f, 1f, 1f, 3f, 3f, 4f, 4f, 3f, 2f, 2f, 4f, 5f, 5f, 2f, 2f},
            {3f, 3f, 4f, 4f, 3f, 3f, 4f, 3f, 5f, 4f, 2f, 2f, 5f, 5f, 3f, 3f, 2f, 2f, 8f, 8f, 6f, 6f, 5f, 5f, 6f, 7f, 5f, 5f, 4f, 1f},
            {2f, 2f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 5f, 5f, 2f, 2f, 5f, 5f, 5f, 5f, 4f, 4f, 6f, 6f, 2f, 3f, 4f, 2f, 2f, 2f, 2f, 2f},
            {2f, 2f, 5f, 5f, 4f, 4f, 2f, 4f, 2f, 4f, 2f, 2f, 4f, 4f, 2f, 2f, 3f, 3f, 5f, 5f, 4f, 4f, 4f, 3f, 4f, 4f, 3f, 3f, 3f, 3f},
            {1f, 1f, 2f, 2f, 3f, 3f, 3f, 2f, 2f, 3f, 4f, 4f, 2f, 2f, 3f, 3f, 5f, 5f, 4f, 4f, 3f, 4f, 3f, 4f, 5f, 6f, 3f, 3f, 3f, 3f},
            {2f, 2f, 5f, 5f, 4f, 4f, 5f, 4f, 5f, 4f, 6f, 6f, 3f, 3f, 2f, 2f, 3f, 3f, 5f, 5f, 8f, 8f, 8f, 9f, 5f, 7f, 5f, 6f, 4f, 2f},
            {1f, 1f, 2f, 2f, 2f, 2f, 3f, 1f, 2f, 2f, 1f, 1f, 2f, 2f, 1f, 1f, 5f, 5f, 6f, 5f, 6f, 5f, 6f, 9f, 9f, 10f, 10f, 8f, 6f, 3f},
            {3f, 3f, 6f, 6f, 5f, 7f, 5f, 8f, 5f, 9f, 6f, 6f, 4f, 4f, 3f, 3f, 2f, 2f, 8f, 8f, 6f, 6f, 5f, 5f, 6f, 7f, 5f, 5f, 4f, 4f},
            {1f, 1f, 2f, 2f, 3f, 3f, 3f, 2f, 2f, 4f, 6f, 6f, 2f, 2f, 1f, 1f, 5f, 5f, 4f, 4f, 6f, 6f, 2f, 3f, 4f, 2f, 2f, 2f, 2f, 2f},
            {4f, 4f, 6f, 6f, 7f, 7f, 5f, 8f, 6f, 7f, 6f, 6f, 5f, 5f, 3f, 3f, 4f, 4f, 2f, 2f, 5f, 6f, 8f, 7f, 8f, 6f, 5f, 4f, 3f, 3f},
            {1f, 1f, 3f, 3f, 4f, 4f, 3f, 2f, 2f, 4f, 5f, 5f, 2f, 2f, 1f, 1f, 3f, 3f, 5f, 5f, 4f, 4f, 4f, 3f, 4f, 4f, 3f, 3f, 3f, 3f},
            {2f, 2f, 8f, 8f, 6f, 6f, 5f, 5f, 6f, 7f, 5f, 5f, 4f, 4f, 3f, 3f, 2f, 2f, 5f, 5f, 5f, 5f, 4f, 4f, 6f, 4f, 2f, 2f, 3f, 3f},
            {1f, 1f, 4f, 4f, 6f, 6f, 2f, 3f, 4f, 2f, 2f, 2f, 2f, 2f, 1f, 1f, 3f, 3f, 5f, 5f, 6f, 6f, 7f, 8f, 9f, 10f, 10f, 8f, 6f, 2f},
            {2f, 2f, 6f, 6f, 7f, 7f, 4f, 4f, 7f, 4f, 3f, 3f, 4f, 4f, 2f, 2f, 4f, 4f, 5f, 5f, 6f, 6f, 6f, 9f, 9f, 10f, 8f, 8f, 6f, 3f},
            {4f, 4f, 5f, 5f, 6f, 6f, 7f, 9f, 9f, 10f, 10f, 10f, 6f, 6f, 3f, 3f, 2f, 2f, 5f, 5f, 5f, 5f, 4f, 4f, 6f, 4f, 2f, 2f, 3f, 3f},
            {4f, 4f, 2f, 2f, 5f, 6f, 8f, 7f, 8f, 6f, 5f, 4f, 3f, 3f, 2f, 2f, 5f, 5f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 5f, 5f, 2f, 2f},
            {3f, 3f, 5f, 5f, 4f, 4f, 4f, 3f, 4f, 4f, 3f, 3f, 3f, 3f, 2f, 2f, 2f, 2f, 5f, 5f, 4f, 4f, 2f, 4f, 2f, 4f, 2f, 2f, 1f, 1f},
            {2f, 5f, 6f, 6f, 3f, 4f, 3f, 4f, 5f, 6f, 3f, 3f, 3f, 3f, 2f, 2f, 4f, 4f, 3f, 3f, 4f, 3f, 5f, 4f, 2f, 2f, 5f, 5f, 3f, 3f},
            {3f, 3f, 5f, 5f, 4f, 4f, 2f, 2f, 3f, 4f, 2f, 2f, 3f, 3f, 2f, 2f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 5f, 5f, 2f, 2f, 1f, 1f},
            {3f, 5f, 3f, 3f, 5f, 5f, 4f, 4f, 5f, 3f, 6f, 6f, 3f, 4f, 3f, 2f, 3f, 3f, 4f, 4f, 3f, 3f, 4f, 3f, 5f, 4f, 2f, 2f, 2f, 1f},
            {4f, 4f, 1f, 1f, 3f, 3f, 6f, 7f, 8f, 8f, 7f, 7f, 3f, 3f, 3f, 3f, 5f, 5f, 3f, 3f, 4f, 4f, 4f, 3f, 4f, 2f, 5f, 5f, 2f, 2f},
            {3f, 3f, 5f, 5f, 8f, 8f, 8f, 9f, 5f, 7f, 5f, 6f, 4f, 4f, 2f, 2f, 4f, 4f, 5f, 5f, 6f, 6f, 6f, 9f, 9f, 10f, 8f, 8f, 6f, 3f},
            {1f, 3f, 6f, 5f, 6f, 5f, 6f, 9f, 9f, 10f, 10f, 10f, 8f, 6f, 4f, 3f, 2f, 2f, 5f, 5f, 5f, 5f, 4f, 4f, 6f, 4f, 2f, 2f, 3f, 3f},
            {2f, 4f, 5f, 4f, 5f, 5f, 6f, 8f, 6f, 4f, 3f, 2f, 3f, 3f, 4f, 4f, 3f, 3f, 10f, 10f, 7f, 7f, 10f, 7f, 10f, 7f, 7f, 7f, 3f, 3f},
            {3f, 3f, 5f, 5f, 6f, 6f, 7f, 8f, 9f, 10f, 10f, 10f, 7f, 6f, 3f, 3f, 1f, 1f, 2f, 2f, 4f, 4f, 5f, 2f, 2f, 4f, 6f, 6f, 3f, 3f},

    };

    /**
     * fitness test 模式的速度表
     * ftModeTableSpeed[0]是公制
     * ftModeTableSpeed[1]是英制
     */
    public static final float ftModeTableSpeed[][] = {
            {7.2f, 7.2f, 7.2f, 7.2f, 7.2f, 8.0f, 8.0f, 8.0f, 8.0f, 8.0f, 8.0f, 8.0f, 8.0f, 8.8f, 8.8f,
                    8.8f, 8.8f, 8.8f, 8.8f, 8.8f, 8.8f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 10.4f,
                    10.4f, 10.4f, 10.4f, 10.4f, 10.4f, 10.4f, 10.4f, 11.2f, 11.2f, 11.2f, 11.2f, 8.0f, 8.0f, 8.8f, 8.8f,
                    8.8f, 8.8f, 8.8f, 8.8f, 8.8f, 8.8f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 9.6f, 10.4f,},

            {4.5f, 4.5f, 4.5f, 4.5f, 4.5f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.5f, 5.5f,
                    5.5f, 5.5f, 5.5f, 5.5f, 5.5f, 5.5f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.5f,
                    6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 7.0f, 7.0f, 7.0f, 7.0f, 5.0f, 5.0f, 5.5f, 5.5f,
                    5.5f, 5.5f, 5.5f, 5.5f, 5.5f, 5.5f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.0f, 6.5f,}
    };
    /**
     * fitness test 模式的扬升表
     */
    public static final float ftModeTableIncline[] = {
            0f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 8f, 8f, 8f, 8f, 8f,
            8f, 8f, 8f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f, 4f, 4f, 4f, 4f, 4f, 4f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 8f, 8f, 8f, 8f, 8f
    };
}