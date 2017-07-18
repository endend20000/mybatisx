package org.cabbage.mybatisx.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.cabbage.mybatisx.core.entity.BaseEntity;
/**
 * 
 * @author GeZhangyuan
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ManyToMany {
	/**
	 * 中间表
	 * @return
	 */
	Class<? extends BaseEntity> table();
}
