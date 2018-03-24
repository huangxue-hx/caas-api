package com.harmonycloud.common.util;

import org.apache.commons.lang.StringUtils;

import java.security.SecureRandom;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-3-2
 * @Modified
 */
public class DesUtil {
    private static String DEFAULT_KEY = "DES_KEY_8";

    /**
     * 加密
     *
     * @param src String
     * @param key String
     * @return String
     */
    public static String encrypt(String src, String key) throws Exception {
        if (StringUtils.isBlank(src)) {
            return null;
        }
        String _key = StringUtils.isBlank(key) ? DEFAULT_KEY : key;
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(_key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
        return parseByte2HexStr(cipher.doFinal(src.getBytes()));
    }

    /**
     * 解密
     *
     * @param src  String
     * @param key String
     * @return String
     * @throws Exception
     */
    public static String decrypt(String src, String key) throws Exception {
        if (StringUtils.isBlank(src)) {
            return null;
        }
        String _key = StringUtils.isBlank(key) ? DEFAULT_KEY : key;
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(_key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        try {
            return new String((cipher.doFinal(parseHexStr2Byte(src))));
        }catch(Exception e){
            return null;
        }
    }

    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}
