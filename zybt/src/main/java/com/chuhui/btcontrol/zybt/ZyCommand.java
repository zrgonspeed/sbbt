package com.chuhui.btcontrol.zybt;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2020/03/25
 */
public class ZyCommand {

    public static final int PKG_HEAD = 0xAA;

    public static final int PKG_LEN = 32;

    public static final byte CONNECT_STATE = 0x71;

    public static final byte RESULT_OK = 0x00;

    /************************  Fitness Machine Control Point  ****************/
    public static final byte REQUEST_CONTROL = 0X00;
    public static final byte FTMS_RESET = 0X01;
    public static final byte SET_TARGET_SPEED = 0X02;
    public static final byte SET_TARGET_INCLINATION = 0X03;
    public static final byte SET_TARGET_RESISTANCE_LEVEL = 0X04;
    public static final byte SET_TARGET_POWER = 0X05;
    public static final byte SET_TARGET_HEART_RATE = 0X06;
    public static final byte START_OR_RESUME = 0X07;
    public static final byte STOP_OR_PAUSE = 0X08;
    public static final byte SET_TARGETED_NUMBER_OF_STEPS = 0X0A;
    public static final byte SET_TARGETED_NUMBER_F_STRIDES = 0X0B;
    public static final byte SET_TARGETED_DISTANCE = 0X0C;
    public static final byte SET_TARGETED_TRAINING_TIME = 0X0D;


    /************************  Training Status  ********************************/
    public static final byte TRAINING_OTHER = 0X00;
    public static final byte TRAINING_IDLE = 0X01;
    public static final byte TRAINING_WARMING_UP = 0X02;
    public static final byte TRAINING_FITNESS_TEST_MODE = 0X08;
    public static final byte TRAINING_COOL_DOWN = 0X0B;
    //quick start
    public static final byte TRAINING_MANUAL_MODE = 0X0D;
    /** 运动前（3-2-1）*/
    public static final byte TRAINING_PRE_WORKOUT = 0X0E;
    /** 运动结束*/
    public static final byte TRAINING_POST_WORKOUT = 0X0F;


    /*******************************    Fitness Machine status    *******************************/
    public static final byte CTRL_CMD_RESET = 0x01;
    public static final byte CTRL_CMD_STOP_PAUSE = 0x02;
    public static final byte CTRL_CMD_STOP_SAFETY_KEY = 0x03;
    public static final byte CTRL_CMD_START_RESUME = 0x04;
    public static final byte CTRL_CMD_SPEED_CHANGE = 0x05;
    public static final byte CTRL_CMD_INCLINE_CHANGE = 0x06;
    public static final byte CTRL_CMD_LEVEL_CHANGE = 0x07;
    public static final byte CTRL_CMD_POWER_CHANGE = 0x08;
    public static final byte CTRL_CMD_HEART_RATE_CHANGE = 0x09;
}