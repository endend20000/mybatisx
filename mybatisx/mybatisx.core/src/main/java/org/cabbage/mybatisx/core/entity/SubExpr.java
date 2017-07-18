package org.cabbage.mybatisx.core.entity;
/**
 * 
 * @author GeZhangyuan
 *
 */
import java.util.ArrayList;
import java.util.List;

import org.cabbage.mybatisx.core.builder.ExprBuilder;

public class SubExpr {
	List<String> field=new ArrayList<String>();
	ExprBuilder expr;
	public List<String> getField() {
		return field;
	}
	public void setField(List<String> field) {
		this.field = field;
	}
	public ExprBuilder getExpr() {
		return expr;
	}
	public void setExpr(ExprBuilder expr) {
		this.expr = expr;
	}
	public SubExpr(List<String> field,ExprBuilder expr){
		this.field=field;
		this.expr=expr;
	}
	public SubExpr() {
	}
}
