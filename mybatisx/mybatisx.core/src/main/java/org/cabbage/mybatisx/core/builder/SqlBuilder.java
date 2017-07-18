package org.cabbage.mybatisx.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.cabbage.mybatisx.core.entity.BaseEntity;
import org.cabbage.mybatisx.core.exception.CqlException;
import org.cabbage.mybatisx.core.exception.EntityException;
import org.cabbage.mybatisx.core.utils.*;
/**
 * 
 * @author GeZhangyuan
 *
 */
public class SqlBuilder {
	StringBuilder joinSql = new StringBuilder("");
	String limitSql = "";

	private Class<?> mainEntityclazz;
	
	private List<List<TableDetail>> tdLists=new ArrayList<List<TableDetail>>();

	public SqlBuilder() {
	}
	
	public SqlBuilder(Class<?> mainEntityclazz) {
		this.mainEntityclazz=mainEntityclazz;
	}
	
	public SqlBuilder getDetail(Class<? extends BaseEntity> table,String joinColumn) throws Exception {
		String tableName=table.getSimpleName() ;	
		String[] tables=joinSql.toString().split("join "+tableName+" ");
		String tableAlias=tableName;
		if(tables.length>1){
			tableAlias+=(tables.length-1);
		}

		Field field=table.getDeclaredField(joinColumn);
		Class<?> clazz=field.getType();
		String detailTable=null;
		if(BaseEntity.class.isAssignableFrom(clazz)){
			detailTable=field.getType().getSimpleName();
		}else if(List.class.isAssignableFrom(clazz)){
			detailTable=ObjectUtils.getGenerics(field).getSimpleName();
		}else{
			throw new EntityException("entity关联字段类型错误");
		}
		String[] detailTables=joinSql.toString().split("join "+detailTable+" ");
		String detailTableAlias=detailTable+detailTables.length;
		
		joinSql.append(" join "+ detailTable +" as " +detailTableAlias+ " on "+tableAlias+"."+ joinColumn);
		return this;
	}
	
	/**
	 * 正序 与getDetail配合使用
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> SqlBuilder orderAsc(String propName) throws Exception{
		orderFunction(propName+" asc");
		return this;
	}	
	
	/**
	 * 倒序与getDetail配合使用
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> SqlBuilder orderDesc(String propName) throws Exception{
		orderFunction(propName+" desc");
		return this;
	}
	/**
	 * 中文正序排列与getDetail配合使用
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> SqlBuilder orderChAsc(String propName) throws Exception{
		orderFunction(propName+" chasc");
		return this;
	}	
	/**
	 * 中文倒序排列与getDetail配合使用
	 * @param propName
	 * @return
	 * @throws Exception
	 */
	public <T> SqlBuilder orderChDesc(String propName) throws Exception{
		orderFunction(propName+" chdesc");
		return this;
	}
	
	private void orderFunction(String expr){
		String[] orderStrs=joinSql.toString().split(" order by ");
		String lastOrder = orderStrs[orderStrs.length-1].trim();
		if(orderStrs.length>1 && (lastOrder.split(" ").length==2||lastOrder.split(",").length>1)){//判断是否是该表第一个order语句
			joinSql.append(","+expr);
		}else{
			joinSql.append(" order by "+expr);
		}
	}
	
	/**
	 * 查找实体中包含的对象
	 * @param columns
	 * @return
	 * @throws Exception
	 */
	public SqlBuilder include(String... columns) throws Exception {
		if(mainEntityclazz==null){
			throw new CqlException("mainEntityclazz未定义");
		}
		
		List<TableDetail> tdList=new ArrayList<TableDetail>();
		tdList.add(new TableDetail(mainEntityclazz,mainEntityclazz.getSimpleName(),null));
		
		for(String column:columns){
			generateCql(tdList,column);
		}
		tdLists.add(tdList);
		return this;
	}

	private void generateCql(List<TableDetail> tdList,String column) throws Exception{
		Field field=tdList.get(tdList.size()-1).clazz.getDeclaredField(column);
		if(field==null){
			throw new CqlException("未能找到"+column+"字段");
		}
		Class<?> clazz=ObjectUtils.getRealType(field);

		if(!EntityUtils.isEntity(clazz)){
			throw new CqlException(column+"字段不是关联字段");
		}

		TableDetail td=new TableDetail(clazz,null,field);
		tdList.add(td);

		List<List<TableDetail>>  sames=getSameList(tdList);
		if(sames.size()==0){
			String[] tables=joinSql.toString().split("join "+clazz.getSimpleName()+" ");
			String tableAlias=clazz.getSimpleName()+tables.length;
			td.alias=tableAlias;
			
			joinSql.append(" join "+tdList.get(tdList.size()-1).clazz.getSimpleName()+ " as " +tableAlias+ " on " +
					tdList.get(tdList.size()-2).alias + "."+column);
		}else{
			td.alias=sames.get(0).get(tdList.size()-1).alias;
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
	 * 分页操作
	 * @param skip
	 * @param rows
	 * @return
	 */
	public SqlBuilder limit(int skip, int rows) {
		limitSql = " limit " + skip + " " + rows;
		return this;
	}
	
	/**
	 * 获取sql
	 */
	public String toString() {
		return joinSql.toString() + limitSql;
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
