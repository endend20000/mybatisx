package org.cabbage.mybatisx.core.dao.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.cabbage.mybatisx.core.utils.*;

import org.cabbage.mybatisx.core.annotation.ManyToMany;
import org.cabbage.mybatisx.core.annotation.OneToMany;
import org.cabbage.mybatisx.core.annotation.OneToOne;
import org.cabbage.mybatisx.core.builder.ExprBuilder;
import org.cabbage.mybatisx.core.builder.SqlBuilder;
import org.cabbage.mybatisx.core.cache.CacheOperator;
import org.cabbage.mybatisx.core.dao.BaseMapper;
import org.cabbage.mybatisx.core.dao.CustomizeSqlMapper;
import org.cabbage.mybatisx.core.entity.ResultEntity;
import org.cabbage.mybatisx.core.entity.SubExpr;
import org.cabbage.mybatisx.core.exception.CqlException;
import org.cabbage.mybatisx.core.exception.EntityException;
import org.cabbage.mybatisx.core.utils.EntityUtils;
/**
 * 
 * @author GeZhangyuan
 *
 */
@Service
public class CustomizeSqlMapperImpl implements CustomizeSqlMapper{
	
	@Autowired
	private BaseMapper baseMapper;
	
	@Autowired
	private CacheOperator cacheOperator;
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomizeSqlMapperImpl.class);
	
	@Override
	public <T> ResultEntity<T> select(T entity, String joinSql)
			throws Exception {
		return select(entity,joinSql,(ExprBuilder[])null);
	}
	
	@Override
	public <T> ResultEntity<T> select(T entity,SqlBuilder joinSql,ExprBuilder... exprs)  throws Exception{
		return select(entity,joinSql.toString(),exprs);
	}
	
	@Override
	public <T> ResultEntity<T> select(T entity,SqlBuilder joinSql)  throws Exception{
		return select(entity,joinSql.toString(),(ExprBuilder[])null);
	}
	
	@Override
	public <T> ResultEntity<T> select(T entity, ExprBuilder... exprs)
			throws Exception {
		return select(entity,"",exprs);
	}

	@Override
	public <T> ResultEntity<T> select(T entity) throws Exception {
		return select(entity,"",(ExprBuilder[])null);
	}
	
	@Override
	public <T> T selectOne(T entity,SqlBuilder joinSql)  throws Exception{
		ResultEntity<T> resultEntity=select(entity,joinSql.toString(),(ExprBuilder[])null);
		if(CollectionUtils.isEmpty(resultEntity.getResult())){
			return null;
		}
		if(resultEntity.getResult().size()>1){
			throw new CqlException("预期返回1条记录，但实际返回了"+resultEntity.getResult().size()+"记录");
		}
		return resultEntity.getResult().get(0);
	}
	
	@Override
	public <T> ResultEntity<T> select(T entity,String joinSql,ExprBuilder... exprs)  throws Exception{
		logger.debug("执行自定义sql: "+joinSql);
		joinSql=StringUtils.deleteExcessBlank(joinSql);
		String[] sqlArray= joinSql.split("limit");
		if(sqlArray.length>1){
			exprs=addPageExprs(exprs,sqlArray[1]);
		}
		ResultEntity<T> mainEntityResults=baseMapper.selectList(entity , exprs);
		List<T> mainEntitys=mainEntityResults.getResult();
		if(mainEntitys!=null&&mainEntitys.size()>0&&sqlArray[0].length()>0){
			String[] joinArray= sqlArray[0].split("join");
			getRelationEntitys(joinArray,mainEntitys,getSubExpr(exprs));
		}
		return mainEntityResults;
	};
	
	private ExprBuilder[] addPageExprs(ExprBuilder[] exprs,String pageSql) throws Exception{
		if(exprs==null){
			return null;
		}
		ExprBuilder[] exprsNew=new ExprBuilder[exprs.length+1];
		for(int i=0;i<exprs.length;i++){
			exprsNew[i]=exprs[i];
		}
		exprsNew[exprs.length+1]=getPageExpr(pageSql);
		return 	exprsNew;
	}
	
	private List<SubExpr> getSubExpr(ExprBuilder[] exprs){
		List<SubExpr> subExprs=new ArrayList<SubExpr>();
		if(ArrayUtils.isNotEmpty(exprs)){
			for(ExprBuilder expr:exprs){
				if(expr!=null){
					CollectionUtils.isNotEmpty(expr.getSubExprs());					
					expr.getSubExprs().addAll(subExprs);
				}
			}
		}
		return subExprs;
	};
	
	/**
	 * 得到分页对象
	 * @param limitSql
	 * @return
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	private ExprBuilder getPageExpr(String limitSql) throws Exception{
		String[] limits= StringUtils.split(limitSql, ",");
		return new ExprBuilder().limit(Integer.valueOf(limits[0]),Integer.valueOf(limits[1]));
		 
	}
	
	/**
	 * 得到关联表数据
	 * @param joinArray
	 * @param mainEntitys
	 * @throws Exception
	 */
	private <T> void getRelationEntitys(String[] joinArray,List<T> mainEntitys,List<SubExpr> subExprs) throws Exception{
		Map<String, List<?>> tableValue=new HashMap<String, List<?>>();
		tableValue.put(mainEntitys.get(0).getClass().getSimpleName(), mainEntitys);
		for(int i=1;i<joinArray.length;i++){		
			String join=joinArray[i];
			String[] joinWords = join.trim().split(" on ");
			String[] tableStrs = joinWords[0].trim().split(" ");
			String[] joinOnOrderWords =joinWords[1].split(" order by ");
			String orderSql=null;
			if(joinOnOrderWords.length>1){
				orderSql=joinOnOrderWords[1].trim();
			}
			
			String[] joinOnWords=joinOnOrderWords[0].trim().split("\\.");
			
			List<ExprBuilder> exprs=new ArrayList<ExprBuilder>();
			for(SubExpr subExpr:subExprs){
				String field=CollectionUtils.getLast(subExpr.getField());
				if(field.equals(joinOnWords[1])){
					exprs.add(subExpr.getExpr());
				}
			}

			if(tableStrs.length==1){
				switchJoinType(joinOnWords[1],joinOnWords[0],tableStrs[0],tableValue,orderSql,exprs);
			}else if(tableStrs.length==3){
				switchJoinType(joinOnWords[1],joinOnWords[0],tableStrs[2],tableValue,orderSql,exprs);
			}else{
				throw new CqlException("自定义sql格式错误");
			}
		}
	}
	
	/**
	 * 选择连接的方式
	 * @param joinFieldName
	 * @param joinTableName
	 * @param tableValue
	 * @throws Exception
	 */
	private void switchJoinType(String joinFieldName,String joinTableName,String tableAlias,Map<String, List<?>> tableValue,
			String orderSql,List<ExprBuilder> exprs) throws Exception{
		List<?> results=tableValue.get(joinTableName);
		if(results==null){
			throw new CqlException("错误的表名/别名"+joinTableName);
		}
		
		List<Object> values=new ArrayList<Object>();
		if(CollectionUtils.isNotEmpty(results)){
			Field field=null;
			for(Object result:results){
				if(result!=null){
					field=results.get(0).getClass().getDeclaredField(joinFieldName);
				}
			}

			if(field==null){
				tableValue.put(tableAlias, values);
				return;
				//throw new CqlException("join field不存在");
			}				
			OneToOne oneToOneAnno=field.getAnnotation(OneToOne.class);
			OneToMany oneToManyAnno=field.getAnnotation(OneToMany.class);
			ManyToMany manyToManyAnno=field.getAnnotation(ManyToMany.class);

			field.setAccessible(true);
			for(Object result:results){
				if(result==null){
					continue;
				}
				if(EntityUtils.isEntity(field.getType())){
					values.addAll(entityJoin(result,field,tableValue));
				}else if(oneToOneAnno!=null){
					oneToOneJoin(result,field,tableValue);
				}else if(oneToManyAnno!=null){
					values.addAll(oneToManyJoin(result,field,tableValue,oneToManyAnno,orderSql,exprs));
				}else if(manyToManyAnno!=null){
					values.addAll(manyToManyAnnoJoin(result,field,tableValue,manyToManyAnno,orderSql,exprs));
				}else{
					throw new EntityException("join Entity的 field缺少相应注解");
				}
			}
		}
		tableValue.put(tableAlias, values);
	}
	
	/**
	 * 外键连接
	 * @param parentEntity
	 * @param parentField
	 * @param tableValue
	 * @throws Exception
	 */
	private List<Object> entityJoin(Object parentEntity,Field parentField,Map<String, List<?>> tableValue) throws Exception{
		Object entity=parentField.get(parentEntity);
		if(entity!=null){
			entity=baseMapper.selectOne(entity);	
			parentField.set(parentEntity, entity);
			List<Object> values=new ArrayList<Object>();
			values.add(entity);
			return values;
		}
		return null;	
	}
	
	/**
	 * 一对一映射
	 * @param parentEntity
	 * @param field
	 * @param tableValue
	 * @throws Exception
	 */
	private void oneToOneJoin(Object parentEntity,Field field,Map<String, List<?>> tableValue) throws Exception{
		throw new Exception("oneToOne注解尚未实现");
	}
	
	/**
	 * 一对多映射
	 * @param parentEntity
	 * @param parentField
	 * @param tableValue
	 * @param oneToManyAnno
	 * @throws Exception
	 */
	private List<Object> oneToManyJoin(Object parentEntity,Field parentField,Map<String, List<?>> tableValue,OneToMany oneToManyAnno,
			String orderSql,List<ExprBuilder> exprs) throws Exception{
		Class<?> entityClass=ObjectUtils.getGenerics(parentField);
		if(entityClass!=null){
                Object entity=entityClass.newInstance();
                
                String fieldName=oneToManyAnno.field();
                Field field=entityClass.getDeclaredField(fieldName);
                
                field.setAccessible(true);
                field.set(entity, parentEntity);
                ExprBuilder orderExpr=getOrderExpr(orderSql,entityClass);
                
                ExprBuilder expr=new ExprBuilder(entityClass);
                for(ExprBuilder itemExpr:exprs){
                	expr.add(itemExpr);
                }

                List<Object> values=baseMapper.selectList(entity,expr,orderExpr).getResult();	
                parentField.set(parentEntity, values);
                return values;
          }
		return null;   
	}
	
	private ExprBuilder getOrderExpr(String orderSqls,Class<?> entityClass) throws Exception{
        ExprBuilder expr=null;
        if(orderSqls!=null){
        	expr=new ExprBuilder(entityClass);
        	for(String orderSql:orderSqls.split(",")){
        	String[] orderStrs = orderSql.trim().split(" ");
	        	switch (orderStrs[1]){
	        	case "asc":expr.orderAsc(orderStrs[0]);break;
	        	case "desc":expr.orderDesc(orderStrs[0]);break;
	        	case "chasc":expr.orderChAsc(orderStrs[0]);break;
	        	case "chdesc":expr.orderChDesc(orderStrs[0]);break;
	        	default :throw new CqlException("排序字符串"+orderStrs[1]+"无效");
	        	}
        	}
        }
        return expr;
	}
	/**
	 * 多对多映射
	 * @param parentEntity
	 * @param parentField
	 * @param tableValue
	 * @param manyToManyAnno
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<?>  manyToManyAnnoJoin(Object parentEntity,Field parentField,Map<String, List<?>> tableValue,ManyToMany manyToManyAnno,
			String orderSql,List<ExprBuilder> exprs) throws Exception{
		Class<?> entityClass=ObjectUtils.getGenerics(parentField);
		if(entityClass!=null){
                Object middleParamEntity=manyToManyAnno.table().newInstance();//Class.forName(className).newInstance();
                Field middleParentField=getFieldByType(middleParamEntity,parentEntity.getClass());
                Field middleChildField=getFieldByType(middleParamEntity,entityClass);
                Class<?> middleChildClass=middleChildField.getType();
                
                middleParentField.set(middleParamEntity, parentEntity);
                
        		List<Object> middleEntitys=baseMapper.selectList(middleParamEntity).getResult();
        		List<Object> ids=new LinkedList<Object>();
        		
        		if(CollectionUtils.isNotEmpty(middleEntitys)){
        			List<Object> childEntitys=new LinkedList<Object>();

	        			//如果内存中有就读内存 如果没有就查数据库
	                    for(Object middleEntity:middleEntitys){
	                    	if(orderSql==null){
	                        	Object childParamEntity=middleChildField.get(middleEntity);
                        		Object childEntity=null;
                				if(CollectionUtils.isEmpty(exprs)&&CollectionUtils.isEmpty(exprs)){
                					childEntity=cacheOperator.get(childParamEntity);
                				}
                				
	                        	if(childEntity==null){
	                        		ids.add(EntityUtils.getId(childParamEntity));
	                        	}else{
	                        		childEntitys.add(childEntity);
	                        	}
	                    	}else{
	                    		Object childParamEntity=middleChildField.get(middleEntity);
	                    		ids.add(EntityUtils.getId(childParamEntity));
	                    	}
	                    }
	                    if(CollectionUtils.isNotEmpty(ids)){
	                        ExprBuilder expr =new ExprBuilder(middleChildClass);
	                        expr.In("id",  ids.toArray(new Object[ids.size()]));
	                        for(ExprBuilder itemExpr:exprs){
	                        	expr.add(itemExpr);
	                        }
	                        
	                        ExprBuilder orderExpr=getOrderExpr(orderSql,middleChildClass);

							List noCacheChildEntitys=baseMapper.selectList(middleChildClass.newInstance(),expr,orderExpr).getResult(); 
	                        cacheOperator.set(noCacheChildEntitys);
	                        noCacheChildEntitys.addAll(childEntitys);
	                    }
	                    parentField.set(parentEntity, childEntitys);  
	                    return childEntitys;	        		
        		}
          }
		return null;
	}
	
	/**
	 * 获取字段属相
	 * @param entity
	 * @param type
	 * @return
	 * @throws Exception 
	 * @throws SecurityException 
	 */
	private Field getFieldByType(Object entity, Class<?> type) throws Exception {
		Field[] middleFields = entity.getClass().getDeclaredFields();
		for (Field middleField : middleFields) {
			if (EntityUtils.isEntity(middleField.getType()) && middleField.getType() == type) {
				middleField.setAccessible(true);
				return middleField;
			}
		}
		return null;
	}
}
