package org.cabbage.mybatisx.demo.entity;

import org.cabbage.mybatisx.core.annotation.*;
import org.cabbage.mybatisx.core.entity.BaseEntity;


@Table("brand")
public class Brand implements BaseEntity{
	@Primary
	@Field("brid")
	private Integer id;
	
	@Field("name")
    private String name;

	@Field("desc")
    private String desc;

	@Field("code")
    private String code;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
