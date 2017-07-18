package org.cabbage.mybatisx.core.cache;

import java.util.List;
/**
 * 
 * @author GeZhangyuan
 *
 */
public interface CacheOperator {
	 /**
	  * 清空缓存
	  * @param clazz
	  * @throws Exception
	  */
	 <T> void clean(Class<T> clazz) throws Exception ;
	 
	 /**
	  * 通过主键删除一条缓存
	  * @param entity
	  * @throws Exception
	  */
	 <T> void clean(T entity) throws Exception ;
	 
	 /**
	  * 清空缓存
	  * @param tableName
	  * @throws Exception
	  */
	 void clean(String tableName) throws Exception;
	 /**
	  * 获取一条珲春记录
	  * @param entity
	  * @return
	  * @throws Exception
	  */
	 <T> T get(T entity) throws Exception;

	 /**
	  * 插入一条缓存记录
	  * @param entity
	  * @throws Exception
	  */
	 void set(Object entity) throws Exception;
	 
	 /**
	  * 插入多条缓存记录
	  * @param entity
	  * @throws Exception
	  */
	 <T> void set(List<T> entitys) throws Exception;
}
