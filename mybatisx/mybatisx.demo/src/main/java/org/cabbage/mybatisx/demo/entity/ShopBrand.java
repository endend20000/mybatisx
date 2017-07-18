package org.cabbage.mybatisx.demo.entity;

import org.cabbage.mybatisx.core.annotation.Field;
import org.cabbage.mybatisx.core.annotation.Table;
import org.cabbage.mybatisx.core.entity.BaseEntity;

@Table("ShopBrand")
public class ShopBrand implements BaseEntity {
	
	@Field("shId")
	private Shop shop;

	@Field("brId")
	private Brand brand;

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}
}
