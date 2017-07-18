package org.cabbage.mybatisx.core.exception;
/**
 * 自定义sql语法错误
 * @author GeZhangyuan
 *
 */
public class CqlException extends Exception{

	public CqlException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
