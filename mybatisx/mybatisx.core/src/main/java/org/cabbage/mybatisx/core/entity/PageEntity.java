package org.cabbage.mybatisx.core.entity;
/**
 * 
 * @author GeZhangyuan
 *
 */
public class PageEntity {
	private Integer page = 1;
	private Integer rows = 100;
	
	public PageEntity(){
		
	}
	
	public PageEntity(int page,int rows){
		if(page>0){
			this.page=page;
		}
		if(rows>0){
			this.rows=rows;
		}		
	}
	
	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public String toString(){
		StringBuilder sb=new StringBuilder(" limit ");
		sb.append(rows*(page-1)+","+rows);	
		return sb.toString();
	}
}
