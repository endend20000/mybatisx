package org.cabbage.mybatisx.core.dao;
/**
 * 
 * @author GeZhangyuan
 *
 */
import org.cabbage.mybatisx.core.builder.ExprBuilder;
import org.cabbage.mybatisx.core.builder.SqlBuilder;
import org.cabbage.mybatisx.core.entity.ResultEntity;

public interface CustomizeSqlMapper {
	/**
	 * 查找sql语句
	 * @param entity
	 * @param joinSql
	 * @param exprs
	 * @return
	 * @throws Exception
	 */
	<T> ResultEntity<T> select(T entity,String joinSql,ExprBuilder... exprs)  throws Exception;
	
	<T> ResultEntity<T> select(T entity,String joinSql)  throws Exception;
	
	<T> ResultEntity<T> select(T entity,SqlBuilder joinSql,ExprBuilder... exprs)  throws Exception;
	
	<T> ResultEntity<T> select(T entity,SqlBuilder joinSql)  throws Exception;
	
	<T> ResultEntity<T> select(T entity,ExprBuilder... exprs)  throws Exception;
	
	<T> ResultEntity<T> select(T entity)  throws Exception;

	<T> T selectOne(T entity, SqlBuilder joinSql) throws Exception;
}
