package com.zving.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * 用于标明UIFacade类中的方法和内部类的别名，以便于前台调用
 * 
 * @Author 王育春
 * @Date 2010-12-21
 * @Mail wyuch@zving.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Alias {
	/**
	 * 用于标注一个方法或者内部是否是一个独立的别名，即不继承UIFacade的别名设定
	 */
	public boolean alone() default false;

	/**
	 * 方法的别名，可以使用'.'分隔路径，也可以使用'/'分隔路径
	 */
	public String value();
}
