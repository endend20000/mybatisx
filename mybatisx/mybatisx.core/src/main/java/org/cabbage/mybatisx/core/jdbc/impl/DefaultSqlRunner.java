package org.cabbage.mybatisx.core.jdbc.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.Null;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import org.cabbage.mybatisx.core.utils.CollectionUtils;
import org.cabbage.mybatisx.core.utils.EntityUtils;
import org.cabbage.mybatisx.core.cache.CacheOperator;
import org.cabbage.mybatisx.core.dao.impl.BaseMapperImpl;
import org.cabbage.mybatisx.core.entity.ResultEntity;
import org.cabbage.mybatisx.core.jdbc.SqlRunner;

/**
 * @author GeZhangyuan
 */
@Service()
public class DefaultSqlRunner implements SqlRunner , ApplicationContextAware{

	private static final String COLUMN_TOTAL_NAME = "rs_total";

	public static final int NO_GENERATED_KEY = Integer.MIN_VALUE + 1001;

	private Connection connection;
	private TypeHandlerRegistry typeHandlerRegistry;
	private boolean useGeneratedKeySupport;
	
	private static ApplicationContext applicationContext;
	
	private static CacheOperator cacheOperator;
	
	private static final Logger logger = LoggerFactory
			.getLogger(BaseMapperImpl.class);
	
	public DefaultSqlRunner() {}
	
	public DefaultSqlRunner(Connection connection) {
		this.connection = connection;
		this.typeHandlerRegistry = new TypeHandlerRegistry();
	}

	public void setUseGeneratedKeySupport(boolean useGeneratedKeySupport) {
		this.useGeneratedKeySupport = useGeneratedKeySupport;
	}

	
  @Override
  public <T> T selectOne(String sql, List<Object> args,Class<T> clazz) throws Exception {
		PreparedStatement ps = connection.prepareStatement(sql);
		try {
			setParameters(ps, args);
			ResultSet rs = ps.executeQuery();
			List<T> list= getResults(rs, clazz);
			
		    if (list!=null && list.size() > 1) {
		        throw new SQLException("预期返回一条记录但返回了"+list.size()+"条记录");
		      }
		    
		    if(CollectionUtils.isNotEmpty(list)){
		    	cacheOperator.set(list);
		    	return list.get(0);
		    }
		    return null;
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
			}
		}
  }
  /*
   * Executes a SELECT statement that returns multiple rows.
   *
   * @param sql  The SQL
   * @param args The arguments to be set on the statement.
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched.
   * @throws SQLException If statement prepration or execution fails
   */
  public <T> ResultEntity<T> selectList(String sql, List<Object> args,Class<T> clazz,boolean insertCache) throws Exception {
		PreparedStatement ps = connection.prepareStatement(sql+";SELECT FOUND_ROWS() rs_total;");
		try {
			setParameters(ps, args);
			ResultSet rs = ps.executeQuery();
			List<T> list= getResults(rs, clazz);
			if(insertCache){
				cacheOperator.set(list);
			}
			Long total=getTotal(ps);
			return total>=0?new ResultEntity<T>(list, total):new ResultEntity<T>(list);
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
			}
		}
  }

  /*
   * Executes an INSERT statement.
   *
   * @param sql  The SQL
   * @param args The arguments to be set on the statement.
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched.
   * @throws SQLException If statement prepration or execution fails
   */
  public int insert(String sql, List<?> args) throws SQLException {
    PreparedStatement ps;
    if (useGeneratedKeySupport) {
      ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    } else {
      ps = connection.prepareStatement(sql);
    }

    try {
      setParameters(ps, args);
      logger.debug("设置参数完成");
      int effect=ps.executeUpdate();
      logger.debug("执行完成"+effect+"行收到影响");
      return effect;
    } finally {
      try {
        ps.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }
  
  public int insertReturnId(String sql, List<?> args) throws SQLException {
	  int effect = insert(sql,args);
	  if(effect==0){
		  return 0;
	  }
		  ResultSet rs=connection.prepareStatement("SELECT LAST_INSERT_ID() as id").executeQuery();
		  if(rs.next()){
			  return rs.getInt("id");
		  }
	  throw new SQLException("未能查找到插入的id");
	  }

  /*
   * Executes an UPDATE statement.
   *
   * @param sql  The SQL
   * @param args The arguments to be set on the statement.
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched.
   * @throws SQLException If statement prepration or execution fails
   */
  public int update(String sql, List<?> args) throws SQLException {
    PreparedStatement ps = connection.prepareStatement(sql);
    try {
      setParameters(ps, args);
      return ps.executeUpdate();
    } finally {
      try {
        ps.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }

  /*
   * Executes a DELETE statement.
   *
   * @param sql  The SQL
   * @param args The arguments to be set on the statement.
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched.
   * @throws SQLException If statement prepration or execution fails
   */
  public int delete(String sql, List<?> args) throws SQLException {
    return update(sql, args);
  }

  /*
   * Executes any string as a JDBC Statement.
   * Good for DDL
   *
   * @param sql The SQL
   * @throws SQLException If statement prepration or execution fails
   */
  public void run(String sql) throws SQLException {
    Statement stmt = connection.createStatement();
    try {
      stmt.execute(sql);
    } finally {
      try {
        stmt.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }

  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      //ignore
    }
  }
  /**
   * 注入参数
   * @param ps
   * @param args
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
private void setParameters(PreparedStatement ps, List<?> args) throws SQLException {
    for (int i = 0, n = args.size(); i < n; i++) {
      if (args.get(i) == null) {
        throw new SQLException("SqlRunner requires an instance of Null to represent typed null values for JDBC compatibility");
      } else if (args.get(i) instanceof Null) {
        ((Null) args.get(i)).getTypeHandler().setParameter(ps, i + 1, null, ((Null) args.get(i)).getJdbcType());
        logger.debug("null");
      } else {
        @SuppressWarnings("rawtypes")
		TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(args.get(i).getClass());
        if (typeHandler == null) {
          throw new SQLException("SqlRunner could not find a TypeHandler instance for " + args.get(i).getClass());
        } else {
          typeHandler.setParameter(ps, i + 1, args.get(i), null);
          logger.debug(args.get(i).toString());
        }
      }
    }
  }

  /**
   * 生成结果集
   */
  private <T> List<T> getResults(ResultSet rs,Class<T> resultType) throws Exception {
    try {
      List<T> list = new ArrayList<T>();
      List<String> columns = new ArrayList<String>();
      List<TypeHandler<?>> typeHandlers = new ArrayList<TypeHandler<?>>();
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
        columns.add(rsmd.getColumnLabel(i + 1));
        try {
          Class<?> type = Resources.classForName(rsmd.getColumnClassName(i + 1));
          TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(type);
          if (typeHandler == null) {
            typeHandler = typeHandlerRegistry.getTypeHandler(Object.class);
          }
          typeHandlers.add(typeHandler);
        } catch (Exception e) {
          typeHandlers.add(typeHandlerRegistry.getTypeHandler(Object.class));
        }
      }
      //将结果封装成实体对象
      while (rs.next()) {
    	  T entity=resultType.newInstance();
        for (int i = 0, n = columns.size(); i < n; i++) {
          String name = columns.get(i);
          TypeHandler<?> handler = typeHandlers.get(i);
          Field[] fields=entity.getClass().getDeclaredFields();
          for(Field field:fields){
        	  org.cabbage.mybatisx.core.annotation.Field fieldAnno=
        			  field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
        	  
        	  Object value=handler.getResult(rs, name);
        	  
        	  if(fieldAnno!=null&&fieldAnno.value().equals(name)&&value!=null){
        		  field.setAccessible(true);
        		  if(field.getType()==Short.class){
        			  field.set(entity, Short.valueOf(value.toString()));
        		  }else if(field.getType()==Byte.class){
        			  field.set(entity, Byte.valueOf(value.toString()));
        		  }else if(field.getType()==Long.class){
        			  field.set(entity, Long.valueOf(value.toString()));
        		  }else if(field.getType()==BigDecimal.class){
        			  field.set(entity, new BigDecimal(value.toString()));
        		  }else if(field.getType()==Boolean.class){
        			  field.set(entity, value);			  
        		  }else if(field.getType()==Date.class){
        			  Long timstamp=Long.valueOf(value.toString())*1000;
        			  field.set(entity, new Date(timstamp));
        		  }else if(field.getType()==Integer.class){
        			  if(value instanceof Boolean){
        				  field.set(entity, ((boolean)value)?1:0);
        			  }else{
            			  field.set(entity, Integer.valueOf(value.toString()));
        			  }
        		  }else if(EntityUtils.isEntity(field.getType())){
        			  EntityUtils.setEntity(entity, value,field);
        		  }else{
        			  field.set(entity, value.toString());
        		  } 
        	  }
          }
        }
        list.add(entity);
      }
      return list;
    } finally {
      try {
        if (rs != null) rs.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }
  /**
   * 得到结果集总数
   * @param ps
   * @return
   * @throws SQLException
   */
  private long getTotal(PreparedStatement ps) throws SQLException {
	  long total=-1;
	  ResultSet rs=null;
	  ResultSetMetaData rsmd=null;
	  String columnName=null;
	  
	  while(ps.getMoreResults()){
		  rs= ps.getResultSet();
    	  rsmd = rs.getMetaData();
          for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
        	  columnName=rsmd.getColumnLabel(i + 1);
        	  if(columnName.equalsIgnoreCase(COLUMN_TOTAL_NAME)){
        		  while (rs.next()) {
        			  total=rs.getLong(columnName);
		    	  }
        		  break;
        	  }
          }
      }
	  return total;
  }
@Override
public void setApplicationContext(ApplicationContext arg0)
		throws BeansException {
	applicationContext = arg0;
	cacheOperator=(CacheOperator) applicationContext.getBean("redisExecutioner");
}
}
