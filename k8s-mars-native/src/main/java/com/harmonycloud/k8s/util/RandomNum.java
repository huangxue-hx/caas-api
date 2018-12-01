package com.harmonycloud.k8s.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomNum {
	
	public static String randomNumber(Integer unit) throws Exception {
		String res= "";
		for (int i= 0; i<unit;i++) {
			int n = (int) (Math.ceil(new SecureRandom().nextDouble() * 26)) + 96;
			char n1 = (char) (n);
			res = res + n1;
		}
		return res;
	}
	
	public static String getRandomString(int a) {
		String base = "abcdefghijklmnopqrstuvwxyz";
		Random random = new SecureRandom();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < a; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
}
