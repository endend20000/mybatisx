package org.cabbage.mybatisx.demo;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cabbage.mybatisx.core.builder.ExprBuilder;
import org.cabbage.mybatisx.core.builder.SqlBuilder;
import org.cabbage.mybatisx.core.dao.BaseMapper;
import org.cabbage.mybatisx.core.dao.CustomizeSqlMapper;
import org.cabbage.mybatisx.core.entity.ResultEntity;
import org.cabbage.mybatisx.demo.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;


public class DEMO {

	private static CustomizeSqlMapper customizeSqlMapper;

	private static BaseMapper  baseMapper;
	
	private static final Logger logger = LoggerFactory.getLogger(DEMO.class);
	
	public static void main(String[] args) {
	    @SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");	 
	    customizeSqlMapper = (CustomizeSqlMapper)context.getBean(CustomizeSqlMapper.class);
	    baseMapper = (BaseMapper)context.getBean(BaseMapper.class);
	    DEMO demo=new DEMO();
	    try {
			demo.insert();
			demo.insertReturnId();
			demo.update();
			demo.insert();
			demo.selectUseCache();
			demo.selectList();
			demo.select();
			demo.selectOne();
			demo.selectJoin();
			demo.selectUseGetDetail();
			demo.selectUseInclude();
			demo.delete();
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	String brandName="testBrand";
	String brandCode="testCode";
	
	@Transactional(rollbackFor={Exception.class})
	public void insert() throws Exception {
		try {
			Brand b = new Brand();
			b.setName(brandName);
			b.setCode(brandCode);
			int effect=baseMapper.insert(b);
			Assert.isTrue(effect>0, "insert error");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void insertReturnId() throws Exception {
		try {
			Brand b = new Brand();
			b.setName(brandName);
			b.setCode(brandCode);
			
			Brand c = new Brand();
			c.setName(brandName);
			c.setCode(brandCode);

			int effect=baseMapper.insertFillId(b,c);

			Assert.isTrue(effect>0, "insert error");
			Assert.isTrue(b.getId()>0, "can't find id");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void update() throws Exception {
		try {
		Brand b=new Brand();
		b.setDesc("test desc");
		ExprBuilder expr = new ExprBuilder(Brand.class);
		expr.In("code", brandCode).In("name", brandName);
		int effect =baseMapper.update(b,expr);
		Assert.isTrue(effect>0, "update error");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void selectUseCache() throws Exception {
		try {
		//单表查找
		Brand param=new Brand();
		param.setId(2);
		ResultEntity<Brand> brands =baseMapper.selectList(param);
		Assert.isTrue(brands.getTotal()>0, "can't find result");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void selectList() throws Exception {
		try {
		//单表查找
		ExprBuilder expr = new ExprBuilder(Brand.class);
		expr.In("code", brandCode).In("name", brandName).orderAsc("id").orderChDesc("name").limit(0, 2000);
		expr.propEqual("id", "id");
		ResultEntity<Brand> brands =baseMapper.selectList(new Brand(),expr);
		
		//use Cache
		ExprBuilder expr2 = new ExprBuilder(Brand.class);
		expr2.In("code", brandCode).In("name", brandName).orderDesc("id").orderChAsc("name").limit(0, 2000);
		expr2.propEqual("id", "id");
		ResultEntity<Brand> brands2 =baseMapper.selectList(new Brand(),expr);
		Assert.isTrue(brands.getTotal()>0,"can't find result");	
		Assert.isTrue(brands2.getTotal()>0,"can't find result");	
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void select() throws Exception {
		try {
		//单表查找
		logger.debug("单表查找");
		ExprBuilder expr = new ExprBuilder(Brand.class);
		expr.In("code", brandCode).In("name", brandName);
		ResultEntity<Brand> brands =customizeSqlMapper.select(new Brand(),expr);
		Assert.isTrue(brands.getTotal()>0,"can't find result");	
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void selectOne() throws Exception {
		try {
		//单表查找
		logger.debug("查找单挑记录");
		Brand param=new Brand();
		param.setId(1);
		Brand brand =customizeSqlMapper.selectOne(param,new SqlBuilder(Brand.class));
		Assert.isTrue(brand!=null,"can't find result");	
		
		Shop siteParam=new Shop();
		siteParam.setId(1);
		SqlBuilder sb=new SqlBuilder(Shop.class)
				.include("brands")
				.include("pictures")
				.include("address");
		Shop site =customizeSqlMapper.selectOne(siteParam,sb);
		
		Assert.isTrue(site!=null,"can't find result");	


		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void selectJoin() throws Exception {
		try {
			//连接查询
			logger.debug("连接查询");
			Shop shop=new Shop();
			shop.setId(1);
			String entitySql="join Address on Shop.address";	
			ExprBuilder expr2 = new ExprBuilder(Shop.class);
			expr2.isNotNull("id");
		
			ResultEntity<Shop> bizEntitys =customizeSqlMapper.select(shop,entitySql,expr2);
			Assert.isTrue(bizEntitys.getTotal()>0,"can't find result");			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	@Transactional(rollbackFor={Exception.class})
	public void selectJoinMany() throws Exception {
		try {
			//多对对连接查询
			logger.debug("多对对连接查询");
			Shop site=new Shop();
			site.setId(1);
			String manyToManySql="join Brand as br on Shop.brands limit 0,1";
			ResultEntity<Shop> sites =customizeSqlMapper.select(site,manyToManySql);
			Assert.isTrue(sites.getTotal()>0,"can't find result");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	/**
	 * 使用sqlbuild查询 使用getDetail方法 暂时废弃
	 * @throws Exception
	 */
	//@SuppressWarnings("deprecation")
	@Transactional(rollbackFor={Exception.class})
	public void selectUseGetDetail() throws Exception {
		try {
			Shop shop=new Shop();
			shop.setId(1);
			ExprBuilder expr = new ExprBuilder(Shop.class);
			expr.In("id", 1,2,3,4,8);			
			SqlBuilder sb=new SqlBuilder();
			    sb.getDetail(Shop.class, "brands").orderAsc("id");
			
			ResultEntity<Shop> sites =customizeSqlMapper.select(shop,sb,expr);			
			Assert.isTrue(sites.getTotal()>0,"can't find result");					
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
	
	/**
	 * 使用sqlbuild查询 使用include方法
	 * @throws Exception
	 */
	@Transactional(rollbackFor={Exception.class})
	public void selectUseInclude() throws Exception {
		try {
			Shop shop=new Shop();
			shop.setId(1);
			ExprBuilder expr = new ExprBuilder(Shop.class);
			expr.In("brands.id", 1,2,3,4,8).limit(0, 3);
			expr.greater("brands.id",0);
			expr.isNotNull("brands.id");
			expr.In("id", 1,2,3,4,8);	
			expr.simpleFunction("brands.id", ">?", 0);
			expr.limit(0, 2);
			SqlBuilder sb=new SqlBuilder(Shop.class)
				.include("brands")
				.include("pictures")
				.include("address");
			
			ResultEntity<Shop> sites =customizeSqlMapper.select(shop,sb,expr);		
			Assert.isTrue(sites.getResult().size()>0,"can't find result");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}

	@Transactional(rollbackFor={Exception.class})
	public void delete() throws Exception {
		try {
		Brand b=new Brand();
		b.setName(brandName);
		
		ExprBuilder expr = new ExprBuilder(Brand.class);
		expr.In("code", brandCode);
		int effect=baseMapper.delete(b,expr);
		Assert.isTrue(effect>0,"can't delete");
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}
}
