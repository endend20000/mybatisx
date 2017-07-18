package org.cabbage.mybatisx.core.interceptor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.cabbage.mybatisx.core.utils.StringUtils;
import org.cabbage.mybatisx.core.cache.CacheOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Properties;

/**
 * 
 * @author GeZhangyuan
 *
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class,Integer.class }) })
public class StatementInterceptor implements Interceptor,ApplicationContextAware {
    @SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(StatementInterceptor.class);
    
	private static ApplicationContext applicationContext;
	
	private static CacheOperator cacheOperator;
    
	/**
	 * 清空缓存
	 */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql().trim().toLowerCase();
        sql=StringUtils.deleteExcessBlank(sql);        
        String tableName=null;       
        String[] words=sql.split(" ");

        if(ArrayUtils.isNotEmpty(words)){
            if(sql.contains("delete")&&words[0].equals("delete")){
            	tableName=words[2];
            }else if(sql.contains("update")&&words[0].equals("update")){
            	tableName=words[1];
            }
            if(tableName!=null){
                cacheOperator.clean(tableName);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
       // String dialect = properties.getProperty("dialect");
       // logger.info("mybatis intercept dialect:{}", dialect);
    }

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
		cacheOperator=(CacheOperator) applicationContext.getBean("redisExecutioner");
	}
}