package com.zving.framework.annotation.dao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标识DAO类对应的数据库表名
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Table {
	/**
	 * @return 表名
	 */
	public String value();
}
