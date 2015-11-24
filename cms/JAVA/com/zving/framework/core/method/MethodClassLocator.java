package com.zving.framework.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.zving.framework.core.Dispatcher.DispatchException;
import com.zving.framework.core.exception.UIMethodInvokeException;

/**
 * UIMethod类定位器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-6
 */
public class MethodClassLocator implements IMethodLocator {
	protected Class<? extends UIMethod> clazz;

	public MethodClassLocator(Class<? extends UIMethod> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Object execute(Object... args) {
		try {
			UIMethod instance = clazz.newInstance();
			UIMethodBinder.bind(instance, args);
			instance.execute();
		} catch (Exception e) {
			if (e.getCause() != null) {
				if (e.getCause() instanceof DispatchException) {// 需要避免捕获
					throw (DispatchException) e.getCause();
				}
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException) e.getCause();
				}
				throw new UIMethodInvokeException(e.getCause());
			} else {
				throw new UIMethodInvokeException(e);
			}
		}
		return null;
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return clazz.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return clazz.getAnnotation(annotationClass);
	}

	@Override
	public String getName() {
		return clazz.getName();
	}

	@Override
	public Method getMethod() {
		return null;
	}
}
