package org.cabbage.mybatisx.demo.entity;

import java.util.List;

import org.cabbage.mybatisx.core.annotation.*;
import org.cabbage.mybatisx.core.entity.BaseEntity;


/**
 * 
 * @author ZhangJun
 *
 */
@Table("shop")
public class Shop implements BaseEntity {
	@Primary
	@Field("shid")	
	private Integer id;

	@Field("name")
    private String name;

	@Field("adid")
	private Address address;

    @OneToMany(field="shop")
	private List<Picture> pictures;
	
    @ManyToMany(table=ShopBrand.class)
	private List<Brand> brands;
	
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

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<Picture> getPictures() {
		return pictures;
	}

	public void setPictures(List<Picture> pictures) {
		this.pictures = pictures;
	}

	public List<Brand> getBrands() {
		return brands;
	}

	public void setBrands(List<Brand> brands) {
		this.brands = brands;
	}

}
