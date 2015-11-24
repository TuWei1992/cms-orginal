package com.zving.framework.annotation.dao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zving.framework.data.DataTypes;

/**
 * 用于标识DAO类中的字段信息
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Column {
	/**
	 * @return 字段长段，默认为0
	 */
	public int length() default 0;

	/**
	 * @return 是否为非空，默认为false
	 */
	public boolean mandatory() default false;

	/**
	 * @return 字段名，默认使用JAVA中的变量名。如果代码有混淆则必须赋值。
	 */
	public String name() default "";// 默认使用变量名,如果有混淆时务必给本属性赋值

	/**
	 * @return 是否是主键，默认为false
	 */
	public boolean pk() default false;

	/**
	 * @return 字段精度，默认为0
	 */
	public int precision() default 0;

	/**
	 * @return 数据类型，默认为DataTypes.STRING
	 */
	public int type() default DataTypes.STRING;
}
