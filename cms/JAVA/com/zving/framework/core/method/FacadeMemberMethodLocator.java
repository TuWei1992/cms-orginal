package com.zving.framework.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.core.Dispatcher.DispatchException;
import com.zving.framework.core.exception.UIMethodException;
import com.zving.framework.core.exception.UIMethodInvokeException;

/**
 * UIFacade中的成员方法定位器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-6
 */
public class FacadeMemberMethodLocator implements IMethodLocator {
	private Method m;

	public FacadeMemberMethodLocator(Method m) {
		this.m = m;
		if (!UIFacade.class.isAssignableFrom(m.getDeclaringClass())) {
			throw new UIMethodException("Method " + m.getName() + " 's declaring class " + m.getDeclaringClass().getName()
					+ " not inherit from UIFacade");
		}
		if (Modifier.isStatic(m.getModifiers())) {
			throw new UIMethodException("Method " + m.getName() + " in declaring class " + m.getDeclaringClass().getName()
					+ " has modifier 'static'");
		}
		if (!Modifier.isPublic(m.getModifiers())) {
			throw new UIMethodException("Method " + m.getName() + " in declaring class " + m.getDeclaringClass().getName()
					+ " should has modifier 'public'");
		}
	}

	@Override
	public Object execute(Object... args) {
		UIFacade ui = createFacadeInstance();
		try {
			return m.invoke(ui, UIMethodBinder.convertArg(m, args));
		} catch (IllegalArgumentException e) {
			throw new UIMethodInvokeException(e);
		} catch (IllegalAccessException e) {
			throw new UIMethodInvokeException(e);
		} catch (InvocationTargetException e) {
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
	}

	private UIFacade createFacadeInstance() {
		Class<?> c = null;
		c = m.getDeclaringClass();
		if (Current.getUIFacade() != null && Current.getUIFacade().getClass() == c) {
			return Current.getUIFacade();
		} else {
			try {
				UIFacade facade = (UIFacade) c.newInstance();
				Current.setUIFacade(facade);
				return facade;
			} catch (Exception e) {
				throw new UIMethodInvokeException(e);
			}
		}
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return m.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return m.getAnnotation(annotationClass);
	}

	@Override
	public String getName() {
		return m.getDeclaringClass().getName() + "." + m.getName();
	}
	
	@Override
	public Method getMethod() {
		return m;
	}
		
}
