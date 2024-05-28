package com.run.treadmill.autoupdate.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class WvNetworkUtils {
    public static boolean isOnline() {
        if (check8888()) {
            return true;
        }
        if (checkBaidu()) {
            return true;
        }
        return false;
    }

    public static boolean check8888() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress address = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(address, timeoutMs);
            sock.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkBaidu() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            InetAddress ip = InetAddress.getByName("www.baidu.com");
            SocketAddress address = new InetSocketAddress(ip, 80);

            sock.connect(address, timeoutMs);
            sock.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
