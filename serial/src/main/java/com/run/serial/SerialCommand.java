package com.run.serial;

public class SerialCommand {
    /**
     * 超时次数
     */
    final static int TIME_OUT_COUNT = 20;
    /**
     * 当常态包是使用比如是0x5A的时候,参数位只能设置257
     */
    final static int NORMAL_PARAM_SPACE = 257;

    /**
     * 数据包的包头
     */
    public final static int PACK_FRAME_HEADER = 0xFF;
    /**
     * 数据包的包尾
     */
    public final static int PACK_FRAME_END = 0xFE;

    /**
     * 数据包内最大值(不包括包头与包尾)
     */
    public final static int PACK_FRAME_MAX_DATA = 0xFD;

    /**
     * 数据包最大长度限定
     */
    public final static int RECEIVE_PACK_LEN_MAX = 128;

    /**
     * 写控制指令， 向下控写入控制运行状态指令
     */
    public final static byte TX_WR_CTR_CMD = 0x10;
    /**
     * 读控制指令，读取下控最后收到的控制指令
     */
    public final static byte TX_RD_CTR_CMD = 0x11;

    /**
     * 写一个参数，写入下控指定序号的参数
     */
    public final static byte TX_WR_ONE = 0x20;
    /**
     * 读一起参数，读取下控指定序号的参数
     */
    public final static byte TX_RD_ONE = 0x21;
    /**
     * 写若干个参数，写若干个参数到下控
     */
    public final static byte TX_WR_SOME = 0x40;

    /**
     * 读若干个参数，从下控读若干个参数
     */
    public final static byte TX_RD_SOME = 0x41;

    /**
     * 0x00：命令执行成功
     */
    public final static int EXC_SUCCEED = 0x00;
    /**
     * 0x01：命令执行失败
     */
    public final static int EXC_FAILURE = 0x01;

    /**
     * 0x02：功能码不存在
     */
    public final static int EXC_NO_FUN_C = 0x02;

    /**
     * 0x03：数据写入失败
     */
    public final static int EXC_WR_FAILURE = 0x03;

    /**
     * 0x04：数据防写
     */
    public final static int EXC_WR_INHIBIT = 0x04;
    /**
     * 0x05：参数不存在
     */
    public final static int EXC_NO_PARAMETER = 0x05;

    /**
     * 0x06：写数据超过范围
     */
    public final static int EXC_OVER_LENGTH = 0x06;

    /**
     * 0x07：通讯速度过快，命令忽略处理
     */
    public final static int EXC_OVER_SPEED = 0x07;

    /**
     * 0x80：数据校验错误
     */
    public final static int EXC_CRC_ERROR = 0x80;

}
