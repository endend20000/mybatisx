package org.cabbage.mybatisx.demo.entity;

import org.cabbage.mybatisx.core.annotation.Field;
import org.cabbage.mybatisx.core.annotation.Primary;
import org.cabbage.mybatisx.core.annotation.Table;
import org.cabbage.mybatisx.core.entity.BaseEntity;


/**
 * 
 * @author ZhangJun
 *
 */
@Table("address")
public class Address implements BaseEntity {
	
	@Primary
	@Field("adid")
	private Integer id;

	@Field("address")
	private String address;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
