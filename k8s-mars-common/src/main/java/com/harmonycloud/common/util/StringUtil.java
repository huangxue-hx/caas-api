package com.harmonycloud.common.util;

import com.harmonycloud.common.Constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.harmonycloud.common.Constant.CommonConstant.COMMA;

public class StringUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

    /**
	 * MD5加密
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public static String convertToMD5(String string) throws Exception{
		
		if(string==null || string.equals("")){
			return null;
		}
		
		char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = string.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            LOGGER.warn("MD5加密失败", e);
            return null;
        }
	}

    public static String getId() {
        // 通过uuid生成token
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        String id = str.replaceAll(CommonConstant.LINE, CommonConstant.EMPTYSTRING);
        return id;
    }

    public static String valueOf(Object obj){
        return obj == null?null:obj.toString();
    }


    public static List<String> splitAsList(String str, String split) {
        String[] arr;
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        if (StringUtils.isBlank(split)) {
            arr = str.split(COMMA);
        } else {
            arr = str.split(split);
        }
        if (arr != null) {
            return Arrays.asList(arr);
        }
        return Collections.emptyList();
    }
}
