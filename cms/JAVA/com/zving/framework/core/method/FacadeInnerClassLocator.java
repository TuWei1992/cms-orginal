package com.zving.framework.core.method;

import com.zving.framework.Current;
import com.zving.framework.UIFacade;
import com.zving.framework.core.Dispatcher.DispatchException;
import com.zving.framework.core.exception.UIMethodInvokeException;

/**
 * UIFacade类的内部UIMethod类定位器
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-12-6
 */
public class FacadeInnerClassLocator extends MethodClassLocator {

	public FacadeInnerClassLocator(Class<? extends UIMethod> clazz) {
		super(clazz);
	}

	@Override
	public Object execute(Object... args) {
		UIFacade ui = createFacadeInstance();
		try {
			UIMethod instance = (UIMethod) clazz.getConstructors()[0].newInstance(ui);
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

	private UIFacade createFacadeInstance() {
		Class<?> c = null;
		c = clazz.getDeclaringClass();
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
}
