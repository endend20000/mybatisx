package org.cabbage.mybatisx.core.dao.impl;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.cabbage.mybatisx.core.utils.EntityUtils;
import org.cabbage.mybatisx.core.utils.ArrayUtils;
import org.cabbage.mybatisx.core.utils.CollectionUtils;
import org.cabbage.mybatisx.core.utils.StringUtils;
import org.cabbage.mybatisx.core.annotation.Primary;
import org.cabbage.mybatisx.core.annotation.Table;
import org.cabbage.mybatisx.core.builder.ExprBuilder;
import org.cabbage.mybatisx.core.cache.CacheOperator;
import org.cabbage.mybatisx.core.dao.BaseMapper;
import org.cabbage.mybatisx.core.entity.ResultEntity;
import org.cabbage.mybatisx.core.exception.ParameterException;
import org.cabbage.mybatisx.core.jdbc.SqlRunner;
import org.cabbage.mybatisx.core.jdbc.impl.DefaultSqlRunner;

import org.apache.ibatis.jdbc.Null;
/**
 * 
 * @author GeZhangyuan
 *
 */
@Service()
public class BaseMapperImpl implements BaseMapper{
	
	@Autowired
    private SqlSessionFactoryBean sqlSessionFactoryBean;
	
	@Autowired
    private CacheOperator cacheOperator;
	
	private static Log logger = LogFactory.getLog(BaseMapperImpl.class);  
	
	//private static final Logger logger = LogFactory.getLogger(BaseMapperImpl.class);
	
	@Override
	public  int  insert(Object... entitys) throws Exception {
		SqlSession session=getSession();
		try {
			SqlRunner sqlRunner = new DefaultSqlRunner(session.getConnection());
			if(ArrayUtils.isNotEmpty(entitys)){
				String tableName=getTableName(entitys[0]);
				List<Object> parameter=getInsertParam(entitys);
				String fieldSql=getInsertFieldSql(entitys[0]);
				String valueSql=getInsertValueSql(parameter.size(),entitys.length);
				String sql=StringUtils.appendAll("insert into ",tableName,fieldSql,"values",valueSql);
				logger.debug(sql);
				int effect=sqlRunner.insert(sql, parameter);
				logger.debug("插入了"+effect+"行");
				return effect;
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}finally{
			session.close();
			//sqlRunner.closeConnection();
		}
		return 0;
	}
	
	
	@Override
	public  int  insertFillId(Object... entitys) throws Exception {
		SqlSession session=getSession();
		try {
		if(ArrayUtils.isNotEmpty(entitys)){
			int effect=0;
			for(Object entity:entitys){
				    SqlRunner sqlRunner = new DefaultSqlRunner(session.getConnection());
					String tableName=getTableName(entity);
					List<Object> parameter=getInsertParam(entity);
					String fieldSql=getInsertFieldSql(entitys[0]);
					String valueSql=getInsertValueSql(parameter.size(),1);
					String sql=StringUtils.appendAll("insert into ",tableName,fieldSql,"values",valueSql);
					logger.debug(sql);
					int id=sqlRunner.insertReturnId(sql, parameter);
					if(id>0){
						EntityUtils.setId(entity, id);
						effect++;
					}
				}
			logger.debug("插入了"+effect+"行");
			return effect;
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}finally{
			session.close();
		}
		return 0;
	}
	
	private SqlSession getSession() throws Exception {
		SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
		SqlSession session = sqlSessionFactory.openSession();
		return session;
	}
	/**
	 * 得到表明
	 * @param entity
	 * @return
	 */
	private String getTableName(Object entity){
		Table tableAnno=entity.getClass().getAnnotation(Table.class);
		return tableAnno.value();
	}
	
	/**
	 * 生成插入类型的sql的field部分
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private String getInsertFieldSql(Object entity) throws Exception{
		StringBuilder fieldSql=new StringBuilder("(");
		Field [] fields = entity.getClass().getDeclaredFields();
        for(Field field :fields)
        {  
            field.setAccessible(true);
            org.cabbage.mybatisx.core.annotation.Field fieldAnno = 
            		field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
            org.cabbage.mybatisx.core.annotation.Primary primaryAnno = 
            		field.getAnnotation(org.cabbage.mybatisx.core.annotation.Primary.class);            
            //判断是否需要生成sql
            if((primaryAnno!=null&&primaryAnno.autoincrement())||fieldAnno==null){
            	continue;
            }
        	String sqlFieldName= '`'+fieldAnno.value()+'`';
        	fieldSql.append(sqlFieldName).append(",");
        } 
        StringUtils.deleteLastChar(fieldSql);
        fieldSql.append(")");
		return  fieldSql.toString();
	}
	/**
	 * 生成插入sql的值部分
	 * @param paramCount
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	private String getInsertValueSql(int paramCount,int rows) throws Exception{
		if(rows<=0||paramCount<=0){
			throw new ParameterException("参数不能为空");
		}
		StringBuilder value=new StringBuilder("");
		for(int i=0;i<rows;i++){	
			value.append("(");
			for(int j=0;j<paramCount/rows;j++){
				value.append("?,");
			}
			StringUtils.deleteLastChar(value);
			value.append("),");
		}
		StringUtils.deleteLastChar(value);
		return value.toString();
	}
	/**
	 * 得到插入sql的参数
	 * @param entitys
	 * @return
	 * @throws Exception
	 */
	private List<Object> getInsertParam(Object... entitys) throws Exception{
		List<Object> parameter=new LinkedList<Object>();
		for(Object entity:entitys){
			Field [] fields = entity.getClass().getDeclaredFields();
	        for(Field field :fields)
	        {  
	            field.setAccessible(true);
	            org.cabbage.mybatisx.core.annotation.Field fieldAnno = 
	            		field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
	            Primary primaryAnno =field.getAnnotation(Primary.class);            
	            //判断是否需要生成sql
	            if((primaryAnno!=null&&primaryAnno.autoincrement())||fieldAnno==null){
	            	continue;
	            }
	        	if(field.get(entity)!=null){       		
	            	if(EntityUtils.isEntity(field.getType())){
	            		parameter.add(EntityUtils.getId(field.get(entity)));
	            	}else{
	            		parameter.add(field.get(entity));
	            	}
	        	}else{
	        		parameter.add(Null.OBJECT);
	        	}
	        } 
		}
		return parameter;
	}

	@Override
	public <T> int update(T entity) throws Exception {
		return update(entity,(ExprBuilder[])null);
	}

	@Override
	public <T> int update(T entity, ExprBuilder... exprs) throws Exception {
		SqlSession session=getSession();
		try {
			SqlRunner sqlRunner = new DefaultSqlRunner(session.getConnection());
			List<Object> parameters = new LinkedList<>();
			String tableName = getTableName(entity);
			String fieldSql = getUpdateFieldSql(entity, parameters);
			String whereSql = getUpdateWhereSql(entity, parameters, exprs);
			String sql = StringUtils.appendAll("update " + tableName + fieldSql + whereSql);
			logger.debug(sql);
			int effect = sqlRunner.update(sql, parameters);
			logger.debug("修改了" + effect + "行");
			if (effect > 0) {
				clearCache(entity);
			}
			return effect;

		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			session.close();
		}
	}

	private <T> void clearCache(T entity) throws Exception{
		if(EntityUtils.primaryKeyHasValue(entity)){
			cacheOperator.clean(entity);
		}else{
			cacheOperator.clean(entity.getClass());
		}
	}
	
	/**
	 * 得到更新sql的字段部分
	 * @param entity
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	private <T> String getUpdateFieldSql(T entity,List<Object> parameters) throws Exception{
		StringBuilder fieldSql=new StringBuilder(" set ");
		Field [] fields = entity.getClass().getDeclaredFields();
        for(Field field :fields)
        {  
            field.setAccessible(true);
            org.cabbage.mybatisx.core.annotation.Field fieldAnno = 
            		field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
            Primary primaryAnno = field.getAnnotation(Primary.class);
            if(fieldAnno==null||primaryAnno!=null||field.get(entity)==null){
            	continue;
            }
        	String sqlFieldName= '`'+fieldAnno.value()+'`';
        	fieldSql.append(sqlFieldName).append("=?,");
        	
        	if(EntityUtils.isEntity(field.getType())){
        		parameters.add(EntityUtils.getId(field.get(entity)));
        	}else{
        		parameters.add(field.get(entity));
        	}
        } 
        StringUtils.deleteLastChar(fieldSql);
		return  fieldSql.toString();
	}
	
	/**
	 * 得到更新部分条件sql
	 * @param entity
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	private <T> String getUpdateWhereSql(T entity,List<Object> parameters,ExprBuilder... exprs) throws Exception{
		StringBuilder fieldSql=new StringBuilder(" where ");
		Field [] fields = entity.getClass().getDeclaredFields();
		boolean hasValue=false;
        for(Field field :fields)
        {  
            field.setAccessible(true);
            org.cabbage.mybatisx.core.annotation.Field fieldAnno = 
            		field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class); 
            Primary primaryAnno = field.getAnnotation(Primary.class);         
            if(primaryAnno==null||fieldAnno==null||field.get(entity)==null){
            	continue;
            }
        	String sqlFieldName= '`'+fieldAnno.value()+'`';
        	fieldSql.append(" "+sqlFieldName).append(" =? and");
        	parameters.add(field.get(entity));
        	hasValue=true;
        }   
        String expr=getExprSql(exprs,parameters);  
        if(hasValue&&expr.length()==0){
        	StringUtils.deleteLastChar(fieldSql, 3);
        }
        if(!hasValue&&expr.length()==0){
        	return "";
        }
		return  fieldSql.toString()+expr;
	}
	
	/**
	 * 
	 * @param entity
	 * @return
	 * @throws SQLException 
	 */
	public <T> int delete(T entity) throws Exception{
		return delete(entity,(ExprBuilder[])null);
	}
	
	/**
	 * 
	 * @param expr
	 * @return
	 * @throws SQLException 
	 */
	public <T> int delete(T entity,ExprBuilder... exprs) throws Exception{
		SqlSession session=getSession();
		try {
			SqlRunner sqlRunner = new DefaultSqlRunner(session.getConnection());
			List<Object> parameter = new LinkedList<Object>();
			String tableName=getTableName(entity);
			String whereSql=getWhereSql(entity,parameter,exprs);
			String sql=StringUtils.appendAll("delete from ",tableName+whereSql);
			logger.debug(sql);
			int effect=sqlRunner.delete(sql, parameter);
			logger.debug("删除了"+effect+"行");
			if(effect>0){
				clearCache(entity);
			}
			return effect;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			session.close();
		}
	}

	@Override
	public <T> T selectOne(T entity) throws Exception {
		ResultEntity<T> pageResult=useCache(entity);
		if(pageResult!=null&&CollectionUtils.isNotEmpty(pageResult.getResult())){
			return pageResult.getResult().get(0);
		}
		SqlSession session=getSession();
		try {
			SqlRunner sqlRunner = new DefaultSqlRunner(session.getConnection());
			List<Object> parameters=new LinkedList<>();
			String tableName=getTableName(entity);
			String whereSql=getSelectOneWhereSql(entity,parameters);
			String fieldSql=getSelectField(entity);
			String sql=StringUtils.appendAll("select  ",fieldSql," from "+tableName+" where "+whereSql);
			logger.debug(sql);
			@SuppressWarnings("unchecked")
			T e = (T)sqlRunner.selectOne(sql, parameters,entity.getClass());
			return e;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			session.close();
		}
	}
	/**
	 * 得到查询单一记录的条件部分
	 * @param entity
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	private <T> String getSelectOneWhereSql(T entity,List<Object> parameters) throws Exception{
		Field[] fields= entity.getClass().getDeclaredFields();
		StringBuilder sb=new StringBuilder();
		for(Field field:fields){
			org.cabbage.mybatisx.core.annotation.Primary primaryAnno=
					field.getAnnotation(org.cabbage.mybatisx.core.annotation.Primary.class);
			if(primaryAnno!=null){
				//primarys.add(field.getName());
				org.cabbage.mybatisx.core.annotation.Field fieldAnno=
						field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
				
				sb.append("`"+fieldAnno.value()+"`").append("=? and"); 
				field.setAccessible(true);
				if(field.get(entity)==null){
					throw new Exception("主键查询条件不能为空");
				}
				parameters.add(field.get(entity));
			}
		}
		if(sb.length()==0){
			throw new Exception("主键字段未定义");
		}
        StringUtils.deleteLastChar(sb, 3);
		return sb.toString();
	}
	/**
	 * 得到查询sql的field部分
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private <T> String getSelectField(T entity) throws Exception{
		StringBuilder fieldSql=new StringBuilder("");
		Field [] fields = entity.getClass().getDeclaredFields();
        for(Field field :fields)
        {  
            field.setAccessible(true);
            org.cabbage.mybatisx.core.annotation.Field fieldAnno = 
            		field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
         
            if(fieldAnno==null){
            	continue;
            }
        	String sqlFieldName= "`"+fieldAnno.value()+"`";
        	
        	String tableName=getTableName(entity);
        	
        	if(Date.class==field.getType()){
        		fieldSql.append("UNIX_TIMESTAMP(" +tableName+"."+sqlFieldName+ ") as "+sqlFieldName+",");
        	}else{
        		fieldSql.append(tableName+"."+sqlFieldName).append(",");
        	}
        } 
        StringUtils.deleteLastChar(fieldSql);
		return  fieldSql.toString();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> ResultEntity<T> selectList(T entity, ExprBuilder... exprs)  throws Exception{
		ResultEntity<T> pageResult=useCache(entity,exprs);
		String orderSql=getOrderSql(exprs);
		if(orderSql==null){//当有排序时不使用缓存
			if(pageResult!=null){
				return pageResult;
			}
		}
		SqlSession session=getSession();
		try {
			SqlRunner sqlRunner = new DefaultSqlRunner(session.getConnection());
			List<Object> parameters=new LinkedList<>();
			String tableName=getTableName(entity);
			String joinSql=getJoinSql(exprs);
			String whereSql=getWhereSql(entity,parameters,exprs);
			String fieldSql=getSelectField(entity);
			String limitSql=getLimitSql(exprs);			
			String sql=StringUtils.appendAll("select SQL_CALC_FOUND_ROWS ",fieldSql," from "+tableName+" "+joinSql+" "+whereSql+" "+orderSql+limitSql);
			logger.debug(sql);
			boolean needInsertCache=insertCache(entity,exprs);
			return (ResultEntity<T>)sqlRunner.selectList(sql, parameters,entity.getClass(),needInsertCache) ;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			session.close();
		}
	}
	
	private <T> boolean  insertCache(T entity, ExprBuilder[] exprs) throws Exception{
		if(ArrayUtils.isNotEmpty(exprs)){
			return false;
		}
		return EntityUtils.onlyPrimaryKeyHasValue(entity);
	}
	
	private <T> ResultEntity<T> useCache(T entity, ExprBuilder... exprs) throws Exception{
		if(ArrayUtils.isNotEmpty(exprs)){
			return null;
		}
		if(EntityUtils.onlyPrimaryKeyHasValue(entity)){
			T result=cacheOperator.get(entity);
			if(result!=null){
				List<T> results=new ArrayList<T>();
				results.add(result);
				logger.debug("使用缓存加载");
				return new ResultEntity<T>(results,1L);
			}
		}
		return null;
	}
	
	
	private <T> String getJoinSql(ExprBuilder... exprs) throws Exception{
		StringBuilder sb=new StringBuilder("");
		if(exprs!=null){
			for(ExprBuilder expr:exprs){
				if(expr==null){
					continue;
				}
				if(expr.getJoinSql()!=null){
					return expr.getJoinSql().toString();
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * 得到sql的条件部分
	 * @param entity
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	private <T> String getWhereSql(T entity,List<Object> parameters,ExprBuilder... exprs) throws Exception{
		Field[] fields= entity.getClass().getDeclaredFields();
		StringBuilder sb=new StringBuilder(" where ");
		String tableName=EntityUtils.getTableName(entity.getClass());
		boolean hasValue=false;
		for(Field field:fields){
			org.cabbage.mybatisx.core.annotation.Field fieldAnno=
					field.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
			field.setAccessible(true);
			if(fieldAnno!=null&&field.get(entity)!=null){
				sb.append(" "+tableName+".`"+fieldAnno.value()).append("`=? and"); 	
            	if(EntityUtils.isEntity(field.getType())){
            		parameters.add(EntityUtils.getId(field.get(entity)));
            	}else{
            		parameters.add(field.get(entity));
            	}
            	hasValue=true;
			}
		}
		String expr=getExprSql(exprs,parameters);

		if(hasValue&&expr.length()==0){
			StringUtils.deleteLastChar(sb, 3);
		}
		 
        if(!hasValue&&expr.length()==0){
        	return "";
        }
		return sb.toString()+expr;
	}
	
	/**
	 * 获得orderSql
	 * @param exprs
	 * @return
	 * @throws Exception
	 */
	private String getOrderSql(ExprBuilder[] exprs) throws Exception{
		if(exprs!=null){
			for(ExprBuilder expr:exprs){
				if(expr==null){
					continue;
				}
				if(expr.getOrderSql()!=null){
					return expr.getOrderSql();
				}
			}
		}
		return "";
	}
	
	/**
	 * 得到sql的分页部分
	 * @param page
	 * @return
	 * @throws Exception
	 */
	private String getLimitSql(ExprBuilder[] exprs) throws Exception{
		if(exprs!=null){
			for(ExprBuilder expr:exprs){
				if(expr==null){
					continue;
				}
				if(expr.getLimitSql()!=null){
					return expr.getLimitSql();
				}
			}
		}
		return "";
	}
	

	@Override
	public <T> ResultEntity<T> selectList(T entity) throws Exception {
		return selectList(entity,(ExprBuilder[])null);
	}
	
	/**
	 * 生成表达式sql
	 * @param exprs
	 * @param parameters
	 * @return
	 */
	private <T> String getExprSql(ExprBuilder[] exprs,List<Object> parameters){
		if(ArrayUtils.isEmpty(exprs)){
			return "";
		}
		StringBuilder exprSql=new StringBuilder();
		for(ExprBuilder expr:exprs){
			if(expr==null){
				continue;
			}
			if(StringUtils.isNoneEmpty(expr.getSql())){
				exprSql.append(expr.getSql()).append(" or ");
				parameters.addAll(expr.getParams());
			}
		}
		StringUtils.deleteLastChar(exprSql, 3);
		return exprSql.toString();
	}

}
