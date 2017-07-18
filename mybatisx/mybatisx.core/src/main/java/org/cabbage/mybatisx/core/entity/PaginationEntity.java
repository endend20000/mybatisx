package org.cabbage.mybatisx.core.entity;
/**
 * 
 * @author GeZhangyuan
 *
 */
public class PaginationEntity {
	private Integer pageIndex = 1;
	private Integer pageSize = 100;
	
	public PaginationEntity(){
		
	}
	
	public PaginationEntity(int pageIndex,int pageSize){
		if(pageIndex>0){
			this.pageIndex=pageIndex;
		}
		if(pageSize>0){
			this.pageSize=pageSize;
		}		
	}
	
	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder(" limit ");
		sb.append(pageSize*(pageIndex-1)+","+pageSize);	
		return sb.toString();
	}
}
