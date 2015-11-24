package com.zving.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * 用于标明请求参数的校验规则。<br>
 * 默认检查跨站脚本和SQL注入攻击，如果某些项（例如文章内容）不需要检查，则使用ignore声明。<br>
 * 如果某些项需要使用特定的校验规则(例如Email)，则需要使用Rules加以声明。
 * 
 * @Author 王育春
 * @Date 2010-12-21
 * @Mail wyuch@zving.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Verify {
	/**
	 * 所有键值都不校验
	 */
	public boolean ignoreAll() default false;

	/**
	 * 指定哪些键值不需要校验
	 */
	public String ignoredKeys() default "";

	/**
	 * 指定校验规则
	 */
	public String[] value() default {};
}
