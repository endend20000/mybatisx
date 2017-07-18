package org.cabbage.mybatisx.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.jdbc.Null;
import org.cabbage.mybatisx.core.annotation.*;
import org.cabbage.mybatisx.core.entity.*;
import org.cabbage.mybatisx.core.exception.*;
import org.cabbage.mybatisx.core.utils.*;

/**
 * 
 * @author GeZhangyuan
 *
 */
public class ExprBuilder {
	private StringBuilder condition=new StringBuilder();

	private StringBuilder joinSql=new StringBuilder();
	
	private List<List<TableDetail>> tdLists=new ArrayList<List<TableDetail>>();
	
	private List<Object> params=new ArrayList<>();
	
	private String orderSql;

	private String limitSql;
	
	private Class<?> mainClass;
	 
	private List<SubExpr> subExprs=new ArrayList<SubExpr>();

	/**
	 * 构造方法
	 * @param mainClass
	 */
	public ExprBuilder(Class<?> mainClass){
		this.mainClass=mainClass;
	}
	
	public ExprBuilder(){
	}
	
	public void add(ExprBuilder expr){
		addAnd();
		condition.append(expr.getCondition());
		params.addAll(expr.getParams());
	}
	
	public StringBuilder getCondition() {
		return condition;
	}

	/**
	 * in 函数
	 * @param propName
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder In(String propName,@SuppressWarnings("unchecked") T... values) throws Exception{
		addAnd();
		SubExpr subExpr=handlePropNames(propName);
		condition.append(" in (");
		if(ArrayUtils.isEmpty(values)){
			return this;
		}
		for(T value:values){
			condition.append("?,");
			addParameter(value);
		}
		StringUtils.deleteLastChar(condition);
		condition.append(")");
		if(subExpr!=null){
			subExpr.getExpr().In(ArrayUtils.getLast(propName.split("\\.")), values);
			subExprs.add(subExpr);
		}
		return this;
	}
	
	/**
	 * not in 函数
	 * @param propName
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder notIn(String propName,@SuppressWarnings("unchecked") T... values) throws Exception{
		addAnd();
		SubExpr subExpr=handlePropNames(propName);
		condition.append(" not in (");
		if(ArrayUtils.isEmpty(values)){
			return this;
		}
		for(T value:values){
			condition.append("?,");
			addParameter(value);
		}
		StringUtils.deleteLastChar(condition);
		condition.append(")");
		
		if(subExpr!=null){
			subExpr.getExpr().notIn(ArrayUtils.getLast(propName.split("\\.")), values);
			subExprs.add(subExpr);
		}
		return this;
	}
    
	/**
	 * 向sql中添加and字段
	 */
	private void addAnd(){
		if(condition.length()!=0){
			condition.append(" and ");
		}	
	}
	
	/**
	 * 向sql中添加表明 同时生成join语句
	 * @param propName
	 * @throws Exception
	 */
	private SubExpr handlePropNames(String propNames) throws Exception{
		if(mainClass==null){
			throw new CqlException("ExprBuilder 构造函数class 参数未传");
		}
		String[] propStrs=propNames.split("\\.");
		if(propStrs.length>1){
			SubExpr subExpr=generateJoinSqlAndAddColumnsName(propStrs);
			return subExpr;
		}else{
			String columnsName=EntityUtils.getColumnsName(mainClass, propNames);
			String tableName=EntityUtils.getTableName(mainClass);
			condition.append(" "+tableName+"."+columnsName+" ");
			return null;
		}
	}
	


	private SubExpr generateJoinSqlAndAddColumnsName(String[] propStrs) throws Exception{
		List<TableDetail> tdList=new ArrayList<TableDetail>();
		tdList.add(new TableDetail(mainClass,mainClass.getAnnotation(Table.class).value(),null));
		List<String> subFields=new ArrayList<String>();
		
		ExprBuilder expr=null;
		
		for(int i=0;i<propStrs.length-1;i++){
			String propName=propStrs[i];
			
			Field field=tdList.get(tdList.size()-1).clazz.getDeclaredField(propName);
			if(field==null){
				throw new CqlException("未能找到"+propName+"字段");
			}
			Class<?> joinClazz=ObjectUtils.getRealType(field);

			if(!EntityUtils.isEntity(joinClazz)){
				throw new CqlException(propName+"字段不是关联字段");
			}
			
			subFields.add(propName);
			
			TableDetail td=new TableDetail(joinClazz,null,field);
			tdList.add(td);
			List<List<TableDetail>>  sames=getSameList(tdList);			
			
			if(sames.size()!=0){
				td.alias=sames.get(0).get(tdList.size()-1).alias;
			}
			if(sames.size()!=0&&i!=propStrs.length-2){
				continue;
			}
			
			if(sames.size()==0){
				generateJoinSql(joinClazz,tdList.get(tdList.size()-2),td,propName);
			}
			if(i==propStrs.length-2){
				expr=new ExprBuilder(joinClazz);
				String columnsName=EntityUtils.getColumnsName(joinClazz, propStrs[propStrs.length-1]);
				condition.append(" "+td.alias+"."+columnsName+" ");
			}
		}
		
		tdLists.add(tdList);
		return new SubExpr(subFields,expr);
	}
	
	
	private void generateJoinSql(Class<?> joinClazz,TableDetail beforeTd,TableDetail joinTd,String propName) throws Exception{
		String[] tables=joinSql.toString().split("join "+joinClazz.getSimpleName()+" ");
		String tableAlias=joinClazz.getSimpleName()+tables.length;
		joinTd.alias=tableAlias;
		Field beforeField=beforeTd.clazz.getDeclaredField(propName);
		org.cabbage.mybatisx.core.annotation.Field beforeFieldAnno=beforeField.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
		org.cabbage.mybatisx.core.annotation.ManyToMany beforeFieldAnnoMany=beforeField.getAnnotation(org.cabbage.mybatisx.core.annotation.ManyToMany.class);
		org.cabbage.mybatisx.core.annotation.OneToMany beforeFieldAnnoOneToMany=beforeField.getAnnotation(org.cabbage.mybatisx.core.annotation.OneToMany.class);
		
		Table joinTableAnno=joinClazz.getAnnotation(Table.class);				
		String joinTable=joinTableAnno.value();
		
		List<Field> primaryFields= EntityUtils.getPrimaryField(joinClazz);
		
		if(primaryFields.size()!=1){
			throw new EntityException(" 该方法暂不支持多个主键和没有主键 ");
		}
		org.cabbage.mybatisx.core.annotation.Field joinTablePrimaryFieldAnno= primaryFields.get(0).getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);
		
		if(beforeFieldAnno !=null){
			
			joinSql.append(" join "+joinTable+ " as " +tableAlias+" on "+tableAlias+"."+joinTablePrimaryFieldAnno.value()+"="+beforeTd.alias+"."+beforeFieldAnno.value());
		}
		
		if(beforeFieldAnnoMany !=null){
			Table joinTableAnnoMany = beforeFieldAnnoMany.table().getAnnotation(Table.class);
			String joinTableMany = joinTableAnnoMany.value();
			String tableAliasMany=beforeFieldAnnoMany.table().getSimpleName()+tables.length;
			//查询主表主键
			List<Field> beforePrimaryField= EntityUtils.getPrimaryField(beforeTd.clazz);
			String beforePrimaryFieldValue = beforePrimaryField.get(0).getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class).value();
			
			List<Field> joinPrimaryField= EntityUtils.getPrimaryField(joinTd.clazz);
			String joinPrimaryFieldValue = joinPrimaryField.get(0).getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class).value();
			joinSql.append(" join " + joinTableMany + " as " + tableAliasMany + 
					" on " + tableAliasMany + "."+beforePrimaryFieldValue +"="+beforeTd.alias+"."+beforePrimaryFieldValue+
					" join " +joinTable +" as "+tableAlias+" on "+tableAlias+"."+joinTablePrimaryFieldAnno.value()+"="+tableAliasMany+"."+joinPrimaryFieldValue);
		}
		
		if(beforeFieldAnnoOneToMany!=null){
			Field joinField=joinClazz.getDeclaredField(beforeFieldAnnoOneToMany.field());
			org.cabbage.mybatisx.core.annotation.Field beforeIDField=joinField.getAnnotation(org.cabbage.mybatisx.core.annotation.Field.class);

			joinSql.append(" join "+joinTable+ " as " +tableAlias+" on "+tableAlias+"."+joinTablePrimaryFieldAnno.value()+"="+beforeTd.alias+"."+beforeIDField.value());
		}
		
	}

	/**
	 * 查询两个list中内容一样的item
	 * @param tdList
	 * @return
	 */
	private List<List<TableDetail>> getSameList(List<TableDetail>  tdList){
		List<List<TableDetail>>  differents = new ArrayList<List<TableDetail>> ();
		
		for(List<TableDetail> checkTdList:tdLists){
			if(checkTdList.size()<tdList.size()){
				differents.add(checkTdList);
				continue;
			}
			
			for(int i=0;i<tdList.size();i++){
				if((tdList.get(i).field!=null&&checkTdList.get(i).field!=null)&&
						(!tdList.get(i).field.equals(checkTdList.get(i).field))){
					differents.add(checkTdList);
					break;
				}
			}
		}
		List<List<TableDetail>>  sames = new ArrayList<List<TableDetail>> ();
		sames.addAll(tdLists);
		sames.removeAll(differents);
		return sames;
	}

	/**
	 * 设置sql参数
	 * @param params
	 * @throws Exception
	 */
	private void addParameter(Object... params) throws Exception{
		if(ArrayUtils.isEmpty(params)){
			return ;
		}
		for(Object param:params){
			if(param==null){
				param=Null.OBJECT;
			}
			this.params.add(param);
		}
	}
	
	/**
	 * 大于函数
	 * @param propName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder greater(String propName,Object value) throws Exception{
		return simpleFunction(propName,">?",value);
	}
	
	/**
	 * 小于函数
	 * @param propName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder less(String propName,Object value) throws Exception{
		return simpleFunction(propName,"<?",value);
	}
	
	/**
	 * 大于或等于函数
	 * @param propName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder greaterOrEqual(String propName,Object value) throws Exception{
		return simpleFunction(propName,">=?",value);
	}
	
	/**
	 * 小于或等于
	 * @param propName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder lessOrEqual(String propName,Object value) throws Exception{
		return simpleFunction(propName,"<=?",value);
	}
	
	/**
	 * 空
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder isNull(String propName) throws Exception{
		return simpleFunction(propName," is null",(Object[])null);
	}
	
	/**
	 * 非空
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder isNotNull(String propName) throws Exception{
		return simpleFunction(propName," is not null",(Object[])null);
	}
	
	/**
	 * 不等于
	 * @param propName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder notEqual(String propName,Object value) throws Exception{
		return simpleFunction(propName,"!=?",value);
	}
	
	/**
	 * 等于
	 * @param propName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder equal(String propName,Object value) throws Exception{
		return simpleFunction(propName," =?",value);
	}
	
	/**
	 * 在 a和b之间
	 * @param propName
	 * @param smallValue
	 * @param bigValue
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder between(String propName,Object smallValue,Object bigValue) throws Exception{
		return simpleFunction(propName," BETWEEN ? and ? ",smallValue,bigValue);
	}
	
	/**
	 * 模糊查找
	 * @param propName
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder like(String propName,Object value) throws Exception{
		return simpleFunction(propName," like ?",value);
	}

	/**
	 * 简单函数处理
	 * @param propName
	 * @param expr
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder simpleFunction(String propName,String expr,Object... values) throws Exception{
		addAnd();
		SubExpr subExpr=handlePropNames(propName);
		condition.append(expr);
		addParameter(values);
		if(subExpr!=null){
			subExpr.getExpr().simpleFunction(ArrayUtils.getLast(propName.split("\\.")),expr, values);
			subExprs.add(subExpr);
		}
		return this;
	}

	
	/**
	 * 大于函数
	 * @param propName
	 * @param propName2
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder propGreater(String propName,String propName2) throws Exception{
		return twoParamFunction(propName,propName2,">");
	}
	
	/**
	 * 小于函数
	 * @param propName
	 * @param propName2
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder propLess(String propName,String propName2) throws Exception{
		return twoParamFunction(propName,propName2,"<");
	}
	
	/**
	 * 大于或等于函数
	 * @param propName
	 * @param propName2
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder propGreaterOrEqual(String propName,String propName2) throws Exception{
		return twoParamFunction(propName,propName2,">=");
	}

	/**
	 * 小于或等于
	 * @param propName
	 * @param propName2
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder propLessOrEqual(String propName,String propName2) throws Exception{
		return twoParamFunction(propName,propName2,"<=");
	}
	
	/**
	 * 不等于
	 * @param propName
	 * @param propName2
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder propNotEqual(String propName,String propName2) throws Exception{
		return twoParamFunction(propName,propName2,"!=");
	}
	
	/**
	 * 等于
	 * @param propName
	 * @param propName2
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder propEqual(String propName,String propName2) throws Exception{
		return twoParamFunction(propName,propName2," =");
	}
	
	/**
	 * 双参数函数处理
	 * @param propName
	 * @param propName2
	 * @param expr
	 * @return
	 * @throws Exception
	 */
	private <T> ExprBuilder twoParamFunction(String propName,String propName2,String expr) throws Exception{
		addAnd();
		handlePropNames(propName);
		condition.append(expr);
		handlePropNames(propName2);
		return this;
	}
	/**
	 * 正序
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder orderAsc(String propName) throws Exception{
		String columnsName=EntityUtils.getColumnsName(mainClass, propName);
		orderFunction(columnsName+" asc");
		return this;
	}	
	
	/**
	 * 倒序
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder orderDesc(String propName) throws Exception{
		String columnsName=EntityUtils.getColumnsName(mainClass, propName);
		orderFunction(columnsName+" desc");
		return this;
	}
	/**
	 * 中文正序排列
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder orderChAsc(String propName) throws Exception{
		String columnsName=EntityUtils.getColumnsName(mainClass, propName);
		orderFunction("convert("+columnsName+" USING gbk) COLLATE gbk_chinese_ci asc");
		return this;
	}	
	/**
	 * 中文倒序排列
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder orderChDesc(String propName) throws Exception{
		String columnsName=EntityUtils.getColumnsName(mainClass, propName);
		orderFunction("convert("+columnsName+" USING gbk) COLLATE gbk_chinese_ci desc");
		return this;
	}
	/**
	 * order函数
	 * @param expr
	 */
	private void orderFunction(String expr){
		if(StringUtils.isEmpty(orderSql)){
			orderSql="order by "+expr;
		}else{
			orderSql+=(","+expr);
		}
	}
	
	/**
	 * 
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> ExprBuilder limit(int skip,int rows) throws Exception{
		limitSql=" limit "+skip+","+rows;
		return this;
	}
	
	/**
	 * 获取查询sql
	 */
	public String toString(){
		return condition.toString();
	}

	public StringBuilder getSql() {
		return condition;
	}

	public List<Object> getParams() {
		return params;
	}
	
	public String getOrderSql() {
		return orderSql;
	}
	
	public String getLimitSql() {
		return limitSql;
	}
	
	public StringBuilder getJoinSql() {
		return joinSql;
	}
	
	public List<SubExpr> getSubExprs() {
		return subExprs;
	}
	
	public class TableDetail {
		Class<?> clazz;
		String alias;
		Field field;

		public TableDetail(Class<?> clazz, String alias, Field field) {
			this.clazz = clazz;
			this.alias = alias;
			this.field = field;
		}
	}
}
