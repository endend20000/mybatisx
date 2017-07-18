package org.cabbage.mybatisx.core.entity;
/**
 * 
 * @author GeZhangyuan
 *
 */
import java.util.List;

public class ResultEntity<T> {

	public ResultEntity(){
	}
	
	public ResultEntity(List<T> result){
		this.result=result;
	}
	
	public ResultEntity(List<T> result,long total){
		this.result=result;
		this.total=total;
	}
	
	private long total;
	
	private List<T> result;

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}
}
