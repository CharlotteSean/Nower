package com.project.enjoyit.global;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyAlgorithm {
	
	public static String hashMd5(String str) {
		// ��ϢժҪ��
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String password = str;// �����ַ�
		StringBuffer buffer = new StringBuffer();
		byte[] result = digest.digest(password.getBytes());
		for (byte b : result) {
			int number = b & 0xff;// ������׼����
			// ת����16����
			String numberStr = Integer.toHexString(number);
			if (numberStr.length() == 1) {
				buffer.append("0");
			}
			buffer.append(numberStr);
		}
		// MD5���ܽ��
		return buffer.toString();
	}
	
}