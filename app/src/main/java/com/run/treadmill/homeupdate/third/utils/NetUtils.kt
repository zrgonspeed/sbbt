package com.run.treadmill.homeupdate.third.utils

import com.run.treadmill.util.Logger
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

object NetUtils {
    /**
     * 是否能连公网
     */
    fun isOnline(): Boolean {
        if (check8888()) {
            Logger.i("check8888()")
            return true
        }
        if (checkMyNet()) {
            Logger.i("checkMyNet()")
            return true
        }
        return false
    }

    private fun check8888(): Boolean {
        return try {
            val timeOutMs = 1500
            val sock = Socket()
            val sockAddr = InetSocketAddress("8.8.8.8", 53)

            sock.connect(sockAddr, timeOutMs)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun checkMyNet(): Boolean {
        return try {
            val timeOutMs = 1500
            val sock = Socket()
            val myIp = InetAddress.getByName("www.baidu.com")
            val sockAddr = InetSocketAddress(myIp, 80)

            sock.connect(sockAddr, timeOutMs)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }
}