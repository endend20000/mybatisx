package org.cabbage.mybatisx.core.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.ClassUtils;

/**
 * 
 * @author GeZhangyuan
 *
 */
public class BeanUtils extends org.springframework.beans.BeanUtils{
	
	private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);
	
	public static void copyPropertiesIgnoreCase(Object source, Object target){
		copyPropertiesIgnoreCase(source,target,null,(String[])null);
	}
	/**
	 * 复制对象属相忽略大小写
	 * @param source
	 * @param target
	 * @param editable
	 * @param ignoreProperties
	 * @throws BeansException
	 */
	private static void copyPropertiesIgnoreCase(Object source, Object target, Class<?> editable, String... ignoreProperties)
			throws BeansException {
		if(source==null){
			target=null;
			return;
		}
		Class<?> actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
						"] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;

		for (PropertyDescriptor targetPd : targetPds) {
			Method writeMethod = targetPd.getWriteMethod();
			if (writeMethod != null && (ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {				
				Class<?> sourceClass=source.getClass();
				PropertyDescriptor[] sourcePds=getPropertyDescriptors(sourceClass);
				for(PropertyDescriptor sourcePd :sourcePds){
					if(sourcePd.getName().equalsIgnoreCase(targetPd.getName())){
								Method readMethod = sourcePd.getReadMethod();
								if (readMethod != null &&
										ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
									try {
										if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
											readMethod.setAccessible(true);
										}
										Object value = readMethod.invoke(source);
										if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
											writeMethod.setAccessible(true);
										}
										if(value!=null){
											writeMethod.invoke(target, value);
										}
									}
									catch (Throwable ex) {
										logger.error(ExceptionUtils.getStackTrace(ex));
										throw new FatalBeanException(
												"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
							}
						}
					}
				}
			}	
		}
	}
}
