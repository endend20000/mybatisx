package org.cabbage.mybatisx.core.dao;
/**
 * 
 * @author GeZhangyuan
 *
 */
import org.cabbage.mybatisx.core.builder.ExprBuilder;
import org.cabbage.mybatisx.core.entity.ResultEntity;

/**
 * 
 * @author Yijun
 *
 */
public interface BaseMapper {

	/**
	 * 插入
	 * @param entitys
	 * @return
	 * @throws Exception
	 */
	int insert(Object... entitys) throws Exception ;
	
	/**
	 * 插入并且生成id
	 * @param entitys
	 * @return
	 * @throws Exception
	 */
	int insertFillId(Object... entitys) throws Exception;
	
	/**
	 * 修改
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	<T> int update(T entity) throws Exception;
	
	/**
	 * 修改
	 * @param entity
	 * @param exprs
	 * @return
	 * @throws Exception
	 */
	<T> int update(T entity,ExprBuilder... exprs) throws Exception;

	/**
	 * 修改
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	<T> int delete(T entity) throws Exception;
	
	/**
	 * 删除
	 * @param entity
	 * @param exprs
	 * @return
	 * @throws Exception
	 */
	<T> int delete(T entity,ExprBuilder... exprs)  throws Exception;
	
	/**
	 * 删除
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	<T> T selectOne(T entity) throws Exception;
	
	/**
	 * 查询
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	<T> ResultEntity<T> selectList(T entity)  throws Exception;
	
	/**
	 * 查询
	 * @param entity
	 * @param exprs
	 * @return
	 * @throws Exception
	 */
	<T> ResultEntity<T> selectList(T entity, ExprBuilder... exprs)  throws Exception;

}
