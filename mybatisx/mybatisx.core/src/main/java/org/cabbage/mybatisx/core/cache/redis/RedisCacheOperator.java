package org.cabbage.mybatisx.core.cache.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.cabbage.mybatisx.core.utils.CollectionUtils;
import org.cabbage.mybatisx.core.utils.StringUtils;
import org.cabbage.mybatisx.core.annotation.Table;
import org.cabbage.mybatisx.core.cache.CacheOperator;
import org.cabbage.mybatisx.core.dao.impl.BaseMapperImpl;
import org.cabbage.mybatisx.core.exception.EntityException;
import org.cabbage.mybatisx.core.utils.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
/**
 * 
 * @author GeZhangyuan
 *
 */
@Service("redisExecutioner")
public class RedisCacheOperator implements CacheOperator{
	
   @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;
	
    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueOps;
    
    @Resource(name = "redisTemplate")
    private HashOperations<String, String,Object> hashOps;
    
	private static final Logger logger = LoggerFactory.getLogger(BaseMapperImpl.class);
	
	@Override
	public <T> void clean(Class<T> clazz) throws Exception {
		clean(EntityUtils.getTableName(clazz));
	}
	
	@Override
	public void clean(String tableName) throws Exception {
		String key=generateKey(tableName);
		redisTemplate.delete(key);
		logger.debug("清空表缓存："+key);
	}

	@Override
	public <T> void clean(T entity) throws Exception {
		String idExpr=getIdexpr(entity);
		if(idExpr==null){
			return ;
		}
		hashOps.delete(generateKey(EntityUtils.getTableName(entity.getClass())), idExpr);
		logger.debug("清空行缓存："+generateKey(entity.getClass().getName())+":"+idExpr);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(T entity) throws Exception {
		String idExpr=getIdexpr(entity);
		logger.debug("获取缓存"+entity.getClass().getName()+":"+idExpr);
		T result=(T)hashOps.get(generateKey(EntityUtils.getTableName(entity.getClass())), idExpr);
		if(result!=null){
			logger.debug("成功加载缓存");
		}
		return result;
	}

	@Override
	public void set(Object entity) throws Exception {
		String idExpr=getIdexpr(entity);
		if(idExpr==null){
			return ;
		}
		String tableName=EntityUtils.getTableName(entity.getClass());
		String key=generateKey(tableName);
		if(!hashOps.hasKey(key, idExpr)){
			Table table=entity.getClass().getAnnotation(Table.class);
			hashOps.put(key, idExpr, entity);
			redisTemplate.expire(key, table.remain(), TimeUnit.MILLISECONDS);
			logger.debug("redis 插入 "+key+":"+idExpr);
		}
	}
	
	private String getIdexpr(Object entity) throws Exception{
		List<Object> primaryKeys=EntityUtils.getPrimaryKeys(entity);
		if(primaryKeys.size()==0){
			return null;
		}
		StringBuilder idExpr=new StringBuilder("");
		for(Object primary:primaryKeys){
			if(primary==null){
				throw new EntityException("主键不允许为空");
			}
			idExpr.append(primary).append("-");
		}
		StringUtils.deleteLastChar(idExpr);
		return idExpr.toString();
	}

	@Override
	public <T> void set(List<T> entitys) throws Exception {
		if(CollectionUtils.isNotEmpty(entitys)){
			T firstEntity=entitys.get(0);
			String idExprTest=getIdexpr(firstEntity);
			if(idExprTest==null){
				return ;
			}
			Map<String,Object> map=new HashMap<String,Object>();
			for(Object entity:entitys){				
				String idExpr=getIdexpr(entity);					
				map.put(idExpr, entity);
			}
			Table table=firstEntity.getClass().getAnnotation(Table.class);
			String tableName=EntityUtils.getTableName(firstEntity.getClass());
			String key=generateKey(tableName);
			
			hashOps.putAll(key, map);
			redisTemplate.expire(key, table.remain(), TimeUnit.MILLISECONDS);
		}
	}

	private String generateKey(String tableName) throws Exception{
		String server="";//DBSetter.get();
/*		if(server==null){
			throw new ThreadLocalNotFindException("未能找到ThreadLocal变量");
		}*/
		server=(server==null?"":server+"_");
		return server+tableName;
	}
}
