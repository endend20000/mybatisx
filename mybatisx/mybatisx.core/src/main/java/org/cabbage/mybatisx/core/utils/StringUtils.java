package org.cabbage.mybatisx.core.utils;
/**
 * 
 * @author GeZhangyuan
 *
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
	/**
	 * 删除最后一个字节
	 * @param strBuilder
	 */
	public static void deleteLastChar(StringBuilder strBuilder) {
		deleteLastChar(strBuilder, 1);
	}

	/**
	 * 删除最后几个字节
	 * @param strBuilder
	 */
	public static void deleteLastChar(StringBuilder strBuilder, int count) {
		if (count < 1) {
			count = 1;
		}

		if (strBuilder == null || strBuilder.length() == 0) {
			strBuilder = null;
			return;
		}
		strBuilder.delete(strBuilder.length() - count, strBuilder.length());
	}
	
	
	/**
	 * 删除最后的字节
	 * @param str
	 * @param count
	 * @return
	 */
	public static String deleteLastChar(String str, int count) {
		if (count < 1) {
			count = 1;
		}

		if (str == null || str.length() == 0) {
			return null;
		}
		
		return str.substring(0, str.length()-count);
	}
	
	/**
	 * 将多个StringBuilder合并
	 * @param strBuilder
	 */
	public static String appendAll(StringBuilder... strBuilders) {
		StringBuilder all = new StringBuilder();
		for (StringBuilder stringBuilder : strBuilders) {
			all.append(stringBuilder);
		}
		return all.toString();
	}

	/**
	 * 将多个String合并
	 * @param strBuilder
	 */
	public static String appendAll(String... strings) {
		StringBuilder all = new StringBuilder();
		for (String string : strings) {
			all.append(string);
		}
		return all.toString();
	}

	/**
	 * 删除多余的空格
	 * @param strBuilder
	 */
	public static String deleteExcessBlank(String str) {
		StringBuilder sb = new StringBuilder(str = str.trim());
		int blankCount = 0;
		for (int i = 0; i < sb.length(); i++) {
			char ch = sb.charAt(i);
			if (ch == ' ') {
				if (blankCount == 0) {
					blankCount++;
				} else if (blankCount > 0) {
					sb.deleteCharAt(i);
					i--;
				}
			} else {
				blankCount = 0;
			}
		}
		return sb.toString();
	}
	
	/**
	 * 分割字符串同时去掉多余的空格
	 * @param strBuilder
	 */
	public static String[] split(String str, String regex) {
		if (str == null) {
			return new String[0];
		}
		String[] strArray = str.split(regex);
		for (int i = 0; i < strArray.length; i++) {
			String string = strArray[i].trim();
			strArray[i] = string;
		}
		return strArray;
	}
}
