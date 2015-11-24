/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.zving.framework.core.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.zving.framework.core.castor.CastorService;
import com.zving.framework.core.exception.BeanException;
import com.zving.framework.core.exception.BeanGetPropertyException;
import com.zving.framework.core.exception.BeanSetPropertyException;

/**
 * Bean属性，表示bean中的一对getter/setter，或者一个public字段
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-5-7
 */
public class BeanProperty {
	Method readMethod;
	Method writeMethod;
	String name;
	Field field;

	/**
	 * 构造器
	 * 
	 * @param field public字段
	 */
	public BeanProperty(Field field) {
		this.field = field;
		field.setAccessible(true);
		name = field.getName();
		afterNameSet();
	}

	/**
	 * 构造器
	 * 
	 * @param readMethod 属性的读方法
	 * @param writeMethod 属性的写方法
	 */
	public BeanProperty(Method readMethod, Method writeMethod) {
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		if (readMethod != null) {
			name = readMethod.getName();
			if (name.startsWith("get")) {
				name = name.substring(3);
			}
			if (name.startsWith("is")) {
				name = name.substring(2);
			}
		}
		if (writeMethod != null) {
			name = writeMethod.getName();
			if (name.startsWith("set")) {
				name = name.substring(3);
			}
		}
		afterNameSet();
	}

	/**
	 * @param bean Bean实例
	 * @return 指定的Bean实例中当前属性的值
	 */
	public Object read(Object bean) {
		try {
			if (field != null) {
				return field.get(bean);
			} else if (readMethod != null) {
				return readMethod.invoke(bean);
			}
		} catch (Exception e) {
			throw new BeanGetPropertyException(e);
		}
		throw new BeanException("Bean " + bean.getClass().getName() + " 's property [" + name + "] can't be read!");
	}

	/**
	 * 将值设置到指定Bean实例的当前属性上
	 * 
	 * @param bean Bean实例
	 * @param value 属性值
	 */
	public void write(Object bean, Object value) {
		try {
			if (field != null) {
				value = CastorService.toType(value, field.getType());
				field.set(bean, value);
			} else if (writeMethod != null) {
				value = CastorService.toType(value, writeMethod.getParameterTypes()[0]);
				writeMethod.invoke(bean, new Object[] { value });
			} else {
				throw new BeanException("Bean " + bean.getClass().getName() + " 's property [" + name + "] can't be be write!");
			}
		} catch (Exception e) {
			throw new BeanSetPropertyException(e);
		}
	}

	/**
	 * @return 属性值的类型
	 */
	public Class<?> getPropertyType() {
		if (field != null) {
			return field.getType();
		} else if (writeMethod != null) {
			return writeMethod.getParameterTypes()[0];
		} else if (readMethod != null) {
			return readMethod.getReturnType();
		}
		throw new BeanException("Bean's property [" + name + "] not initalized!");
	}

	/**
	 * 属性名设置之后执行本方法
	 */
	private void afterNameSet() {
		if (Character.isUpperCase(name.charAt(0)) && !Character.isLowerCase(name.charAt(1))) {
			return;// 如果前两个字母都大小写，则返回
		}
		// 将首字母小写
		name = name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	/**
	 * @return 属性名
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return 是否可读
	 */
	public boolean canRead() {
		return field != null || readMethod != null;
	}

	/**
	 * @return 是否可写
	 */
	public boolean canWrite() {
		return field != null || writeMethod != null;
	}
}
