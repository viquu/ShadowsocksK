package com.xlvecle.socks5;

/**
 * Created by xingke on 2017/4/7.
 */
public class Utils {

    public static int byteArrayToShort(byte[] b) {
        return (b[1] & 0xFF) |(b[0] & 0xFF) << 8;
    }

}
