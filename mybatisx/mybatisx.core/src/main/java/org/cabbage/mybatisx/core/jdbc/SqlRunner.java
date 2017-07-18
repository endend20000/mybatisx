package org.cabbage.mybatisx.core.jdbc;

import java.sql.SQLException;
import java.util.List;

import org.cabbage.mybatisx.core.entity.ResultEntity;

/**
 * 
 * @author GeZhangyuan
 *
 */
public interface SqlRunner {
	
	void setUseGeneratedKeySupport(boolean useGeneratedKeySupport);
	
	void run(String sql) throws SQLException;
	
	int insert(String sql, List<?> args) throws SQLException;

	int insertReturnId(String sql, List<?> args) throws SQLException;
	
	int update(String sql, List<?> args) throws SQLException;
	
	int delete(String sql, List<?> args) throws SQLException;
	
	<T> T selectOne(String sql, List<Object> args,Class<T> clazz) throws  Exception ;
	
	<T> ResultEntity<T> selectList(String sql, List<Object> args,Class<T> clazz,boolean insertCache) throws Exception ;
	
	void closeConnection();
}
