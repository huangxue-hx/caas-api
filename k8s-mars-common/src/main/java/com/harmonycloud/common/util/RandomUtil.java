package com.harmonycloud.common.util;


import java.util.Random;

/**
 * Created by anson on 17/7/29.
 */
public class RandomUtil {

    public static String getRandomString(int bit){
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bit; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
