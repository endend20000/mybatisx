package org.cabbage.mybatisx.core.utils;

/**
 * 
 * @author GeZhangyuan
 *
 */
public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils{
	public static <T extends Object> T getLast(T[] array){
		if(isEmpty(array)){
			return null;
		}
		return array[array.length-1];
	}
}
