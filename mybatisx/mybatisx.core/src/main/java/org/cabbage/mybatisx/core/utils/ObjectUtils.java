package org.cabbage.mybatisx.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author GeZhangyuan
 *
 */
public class ObjectUtils {
	/**
	 * 得到field的泛型
	 * @param field
	 * @return
	 */
	public static Class<?> getGenerics(Field field){
		Type genericType = field.getGenericType();
		if(genericType instanceof ParameterizedType){
			Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();   
			Class<?> entityClass =  (Class<?>)types[0]; 
			return entityClass;
		}
		return null;
	}
	
	/**
	 * 是否时基础类型
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static boolean isBaseType(Object object) throws Exception{
		if(object==null){
			return false;
		}
		Class<?> clazz=object.getClass();
		return ( clazz.equals(String.class) ||   
				 clazz.equals(Integer.class)||   
		         clazz.equals(Byte.class) ||   
		         clazz.equals(Long.class) ||   
		         clazz.equals(Double.class) ||   
		         clazz.equals(Float.class) ||   
		         clazz.equals(Character.class) ||   
		         clazz.equals(Short.class) ||   
		         clazz.equals(BigDecimal.class) ||   
		         clazz.equals(BigInteger.class) ||   
		         clazz.equals(Boolean.class) ||   
		         clazz.equals(Date.class) ||   
//		         clazz.equals(DateTime.class) ||
		         clazz.equals(String.class) ||
		         clazz.equals(StringBuilder.class) ||
		         clazz.equals(StringBuffer.class) ||
		         clazz.isPrimitive());
		}
	
	/**
	 * 获取字段类型
	 * @param field
	 * @return
	 * @throws Exception
	 */
	public static Class<?> getRealType(Field field) throws Exception{
		if(field==null){
			return null;
		}else if(List.class.isAssignableFrom(field.getType())){
			return getGenerics(field);
		}else{
			return field.getType();
		}
	}
}
