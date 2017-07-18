package org.cabbage.mybatisx.core.utils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.cabbage.mybatisx.core.annotation.Primary;
import org.cabbage.mybatisx.core.annotation.Table;
import org.cabbage.mybatisx.core.entity.BaseEntity;
import org.cabbage.mybatisx.core.exception.EntityException;

/**
 * 
 * @author GeZhangyuan
 *
 */
public class EntityUtils {
	/**
	 * 得到entity的id
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static Object getId(Object entity) throws Exception{
		Field idField=entity.getClass().getDeclaredField("id");
		idField.setAccessible(true);
		Object id=idField.get(entity);
		return id;
	}
	/**
	 * 设置entity的id
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static void setId(Object entity,Object id) throws Exception{
		Field idField=entity.getClass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(entity, id);
	}
	/**
	 * 获取主键列表
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static List<Object> getPrimaryKeys(Object entity) throws Exception{
		Field[] fields=entity.getClass().getDeclaredFields();
		List<Object> primaryKeys=new LinkedList<Object>();
		for(Field field:fields){
			Primary primaryAnno=field.getAnnotation(Primary.class);
			org.cabbage.mybatisx.core.annotation.Field entityAnno=field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
			if(primaryAnno!=null&&entityAnno!=null){
				field.setAccessible(true);
				primaryKeys.add(field.get(entity));
			}
		}
		return primaryKeys;
	}
	
	/**
	 * 获取主键字段
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static List<Field> getPrimaryField(Class<?> clazz) throws Exception{
		Field[] fields=clazz.getDeclaredFields();
		List<Field> primaryFields=new LinkedList<Field>();
		for(Field field:fields){
			Primary primaryAnno=field.getAnnotation(Primary.class);
			if(primaryAnno!=null){
				primaryFields.add(field);
			}
		}
		return primaryFields;
	}
	
	/**
	 * 设置entity的实体属性的id字段
	 * @param entity
	 * @param id
	 * @param field
	 * @throws Exception
	 */
	public static void setEntity(Object entity,Object id,Field field) throws Exception{
  	    if(id==null){
  	    	return ;
  	    }
	  	if(isEntity(field.getType())){
	  		Object fieldEntity=field.getType().newInstance();
	  		Field idField=fieldEntity.getClass().getDeclaredField("id");
	  		idField.setAccessible(true);
//	  		idField.set(fieldEntity, Integer.valueOf(id.toString()));考虑主键id可能是字符串  例如UUID
	  		idField.set(fieldEntity, id);
	  		field.set(entity, fieldEntity);
	  	}else{
	  		field.set(entity, id);
	  	}
	}
	/**
	 * 获取数据库行的名字
	 * @param entityClass
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public static String getColumnsName(Class<?> entityClass,String propName) throws Exception{
		Field field=entityClass.getDeclaredField(propName);
		if(field==null){
			throw new EntityException("该成员变量不存在");
		}
		org.cabbage.mybatisx.core.annotation.Field fieldAnno = field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
		if(fieldAnno==null){
			throw new EntityException("该成员变量没有添加Field注解");
		}
		return fieldAnno.value();
	}
	
	/**
	 * 是否只有主键有值
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static boolean onlyPrimaryKeyHasValue(Object entity) throws Exception{
		boolean onlyPrimaryKeyHasValue=true;
		boolean hasPrimaryKey=false;
		Field[] fields=entity.getClass().getDeclaredFields();		
		for(Field field:fields){
			org.cabbage.mybatisx.core.annotation.Field fieldAnno=
					field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
			Primary primaryAnno=field.getAnnotation(Primary.class);	
			if(fieldAnno!=null){
				field.setAccessible(true);
				Object value=field.get(entity);
				if(primaryAnno!=null){
					hasPrimaryKey=true;
				}
				if(primaryAnno!=null&&value==null){
					onlyPrimaryKeyHasValue=false;
				}
				if(primaryAnno==null&&value!=null){
					onlyPrimaryKeyHasValue=false;
				}
			}
		}
		if(!hasPrimaryKey){
			onlyPrimaryKeyHasValue=false;
		}
		return onlyPrimaryKeyHasValue;
	}
	
	/**
	 * 主键是否有值
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static boolean primaryKeyHasValue(Object entity) throws Exception{
		boolean primaryKeyHasValue=true;
		Field[] fields=entity.getClass().getDeclaredFields();		
		for(Field field:fields){
			Primary primaryAnno=field.getAnnotation(Primary.class);		
			if(primaryAnno!=null){
				field.setAccessible(true);
				Object value=field.get(entity);
				if(value==null){
					primaryKeyHasValue=false;
				}
			}
		}
		return primaryKeyHasValue;
	}
	
	/**
	 * 获取表名
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static String getTableName(Class<?> entity) throws Exception{
		Table table=entity.getAnnotation(Table.class);
		if(table==null){
			return null;
		}
		return table.value();
	}
	
	public static boolean isEntity(Class<?> clazz) throws Exception{
		if(clazz==null){
			return false;
		}
		return BaseEntity.class.isAssignableFrom(clazz);
	}	
}

