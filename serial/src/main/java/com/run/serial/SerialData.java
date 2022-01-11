package com.run.serial;

public class SerialData {

    private SerialData() {

    }

    private static short[] crcNibbleTbl = {
            0x0000, 0x1081, 0x2102, 0x3183,
            0x4204, 0x5285, 0x6306, 0x7387,
            (short) 0x8408, (short) 0x9489, (short) 0xa50a, (short) 0xb58b,
            (short) 0xc60c, (short) 0xd68d, (short) 0xe70e, (short) 0xf78f
    };

    /*********************************************************************************
     输入：pSrcBuf指向求校验和的数据首地址；len数据长度，单位字节。
     输出：校验和
     功能：通过查表法，每4位查一次表，计算pSrcBuf开始，长度为len的数据的CRC校验和。
     接收数据是低位开头，所以采用反转多项式，多项式为0x8408。
     **********************************************************************************/
    protected static synchronized short calCRCByTable(byte[] pSrcBuf, int len) {
        byte crcLower;
        int i = 0;
        short crcReg = 0;

        crcReg = (short) 0xFFFF;
        while (len-- != 0) {
            crcLower = (byte) (crcReg & 0x0F);
            crcReg = (short) ((crcReg >>= 4) & 0x0FFF);
            crcReg ^= crcNibbleTbl[crcLower ^ (pSrcBuf[i] & 0x0F)];
            crcLower = (byte) (crcReg & 0x0F);
            crcReg = (short) ((crcReg >>= 4) & 0x0FFF);
            crcReg ^= crcNibbleTbl[crcLower ^ ((pSrcBuf[i] >> 4) & 0x0F)];
            i++;
        }
        return crcReg;
    }

    /*********************************************************************************
     输入：pSrcBuf，待解包数据首地址；pResultBuf解包后数据首地址；rawPacketlen待解包
     数据长度，单位字节。
     输出：解包结果，为TRUE表示得到一个正确的数据包，为FALSE表示得到一个校验错误的数据包
     功能：将接收到的未经处理的数据包进行解包，解包后计算CRC校验和。
     **********************************************************************************/
    protected static synchronized int comUnPackage(byte[] pSrcBuf, byte[] pResultBuf, int rawPackageLen) {
        int unPackageBufLen;
        short receivedCRC;
        int CRCByCal;
        byte[] pResultBufTemp;
        if (rawPackageLen > 64) {
            return -1;
        }
        if (rawPackageLen < 2) {
            return -1;
        }
        pResultBufTemp = pResultBuf;
        pResultBufTemp[0] = pSrcBuf[0];

        int srcStep = 0;
        int resTemStep = 0;
        srcStep++;
        resTemStep++;

        rawPackageLen -= 2;// 去掉包头包尾的一个字节后，对数据进行拆分。
        unPackageBufLen = 1;

        while (rawPackageLen > 0) {
            if (ConvertData.byteToInt(pSrcBuf[srcStep]) >= SerialCommand.PACK_FRAME_MAX_DATA) {

                srcStep++;
                pResultBufTemp[resTemStep] = ConvertData.intLowToByte(SerialCommand.PACK_FRAME_MAX_DATA + ConvertData.byteToInt(pSrcBuf[srcStep]));
                srcStep++;
                resTemStep++;

                unPackageBufLen++;
                rawPackageLen -= 2;
            } else {
                pResultBufTemp[resTemStep] = pSrcBuf[srcStep];
                srcStep++;
                resTemStep++;

                unPackageBufLen++;
                rawPackageLen--;
            }
        }
        if (unPackageBufLen > 64) {
            return -1;
        }
        receivedCRC = ConvertData.bytesToShortLiterEnd(pResultBuf, unPackageBufLen - 2);
        CRCByCal = calCRCByTable(ConvertData.subBytes(pResultBuf, 1, unPackageBufLen - 1), unPackageBufLen - 3);    // 包头、包尾及CRC不校验
        if (receivedCRC == CRCByCal) {
            // 如果成功，则返回解压后的长度
            pResultBuf[unPackageBufLen] = (byte) SerialCommand.PACK_FRAME_END;
            unPackageBufLen += 1;
            return unPackageBufLen;
        } else {
            return -1;
        }
    }

    /*********************************************************************************
     输入：pSrcBuf，待打包数据首地址；pResultBuf打包后数据首地址；len待打包数据长度，
     ，单位字节。
     输出：打包后，数据包的长度，长度包括包头和包尾。
     功能：将待发送的数据打包成最终要发送的数据。先求包头和校验码之间(不包含包头和校验码)
     的校验码，追加到包尾，接着将FD到FF之间的数据进行拆分，最后求出数据包的长度。
     **********************************************************************************/
    protected static synchronized byte comPackage(byte[] pSrcBuf, byte[] pResultBuf, int len) {
        short crc;
        byte resultLen;
        crc = calCRCByTable(ConvertData.subBytes(pSrcBuf, 1, pSrcBuf.length - 1), len - 1); // 校验码
        byte[] crcByte = ConvertData.shortToBytes(crc);
        pSrcBuf[len] = crcByte[0];
        pSrcBuf[len + 1] = crcByte[1];
        // 加入CRC后，长度增加2
        len += 2;
        // 0xFF
        pResultBuf[0] = pSrcBuf[0];
        int srcStep = 0;
        int resStep = 0;
        srcStep++;
        resStep++;

        // 去掉包头的一个字节后，对数据进行拆分。
        len--;
        resultLen = 1; // oxff
        while (len > 0) {
            if (ConvertData.byteToInt(pSrcBuf[srcStep]) >= SerialCommand.PACK_FRAME_MAX_DATA) {
                pResultBuf[resStep] = ConvertData.intLowToByte(SerialCommand.PACK_FRAME_MAX_DATA);    //当前值拆分为FD+X
                resStep++;    //指针后移
                pResultBuf[resStep] = ConvertData.intLowToByte(ConvertData.byteToInt(pSrcBuf[srcStep]) - SerialCommand.PACK_FRAME_MAX_DATA);    //拆分为X
                srcStep++;    //指针后移
                resStep++;    //指针后移
                resultLen += 2;    //长度加拆分为两个字节
            }
            //正常数据
            else {
                pResultBuf[resStep] = pSrcBuf[srcStep];
                srcStep++;
                resStep++;
                resultLen++;
            }
            len--;
        }
        pResultBuf[resStep] = (byte) SerialCommand.PACK_FRAME_END;//加上包尾
        resultLen++;
//        cmdString = SerialStringUtil.byteArrayToHexString(pResultBuf, resultLen);
        return resultLen;
    }
}
