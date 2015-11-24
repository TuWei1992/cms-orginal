package com.zving.framework.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * UIMethod定位器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-6
 */
public interface IMethodLocator {
	/**
	 * 执行定位器指向的UIMethod
	 * 
	 * @param args 方法参数
	 * @return 执行结果
	 */
	public Object execute(Object... args);

	/**
	 * @param annotationClass 注解类
	 * @return 是否含有指定注解
	 */
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);

	/**
	 * @param annotationClass 注解类
	 * @return 注解在UIMethod上的注解值
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

	/**
	 * @return 定位器所向的方法的全路径
	 */
	public String getName();

	public Method getMethod();
}
