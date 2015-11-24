package com.zving.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * 用于声明执行方法所拥有的权限。<br>
 * 
 * @Author 王育春
 * @Date 2010-12-21
 * @Mail wyuch@zving.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Priv {
	/*
	 * Manager表示后台用户，Member表示前台用户
	 */
	public enum LoginType {
		User, Member;
	}

	/**
	 * 当前操作是否要求己登录，默认要求己登录。
	 */
	public boolean login() default true;

	/**
	 * 当前操作要求的用户类型，默认是后台用户
	 */
	public LoginType loginType() default LoginType.User;

	/**
	 * 当前操作要求用户属性中具有某些值，便如RealName=Test
	 */
	public String userType() default "";

	/**
	 * 权限类型，由业务系统通过扩展机制来处理
	 */
	public String value() default "";
}
