package org.cabbage.mybatisx.demo.entity;

import org.cabbage.mybatisx.core.annotation.*;
import org.cabbage.mybatisx.core.entity.BaseEntity;

@Table("picture")
public class Picture implements BaseEntity {
	@Primary
	@Field("id")
	private Integer id;

	@Field("url")
	private String url;

	@Field("shid")
	private Shop shop;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}
}
