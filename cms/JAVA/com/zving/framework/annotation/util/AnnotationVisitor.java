package com.zving.framework.annotation.util;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.zving.framework.Config;
import com.zving.framework.ConfigLoader;
import com.zving.framework.UIFacade;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.method.UIMethod;
import com.zving.framework.core.scanner.AsmUtil;
import com.zving.framework.core.scanner.BuiltResource;
import com.zving.framework.core.scanner.BuiltResourceScanner;
import com.zving.framework.core.scanner.IBuiltResourceVisitor;
import com.zving.framework.thirdparty.asm.Opcodes;
import com.zving.framework.thirdparty.asm.tree.ClassNode;
import com.zving.framework.thirdparty.asm.tree.MethodNode;
import com.zving.framework.xml.XMLElement;

/**
 * 遍历所有类中ZCF相关的注解
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-7
 */
public class AnnotationVisitor implements IBuiltResourceVisitor {
	protected static long lastTime = 0;
	private static Lock lock = new ReentrantLock();
	private static String UIFACADE;
	private static String ALIAS;
	private static String PRIV;
	private static String UIMETHOD;
	private static final String VALUE = "value";
	private static final String ALONE = "alone";
	private static final String SCHEMA = "com/zving/schema/";
	private static final String FRAMEWORK = "com/zving/framework/";
	private static BuiltResourceScanner scanner;

	@Override
	public String getExtendItemID() {
		return "com.zving.framework.annotation.AnnotationVisitor";
	}

	@Override
	public String getExtendItemName() {
		return "Annotation Visitor";
	}

	@Override
	public boolean match(BuiltResource br) {
		String fullName = br.getFullName();
		if (fullName.indexOf(FRAMEWORK) >= 0 || fullName.indexOf(SCHEMA) >= 0) {
			return false;
		}
		return fullName.endsWith("UI.class") || fullName.endsWith("Method.class") || fullName.indexOf("UI$") > 0;
	}

	private static void init() {
		UIFACADE = UIFacade.class.getName().replace('.', '/');
		ALIAS = Alias.class.getName().replace('.', '/');
		PRIV = Priv.class.getName().replace('.', '/');
		UIMETHOD = UIMethod.class.getName().replace('.', '/');
		scanner = new BuiltResourceScanner(new AnnotationVisitor(), null);
	}

	public static void load() {
		if (lastTime == 0 || Config.isDebugMode() && System.currentTimeMillis() - lastTime > 3000) {// 开发模式下3秒扫描一次
			lock.lock();
			try {
				if (lastTime == 0 || Config.isDebugMode() && System.currentTimeMillis() - lastTime > 3000) {
					if (scanner == null) {
						init();
						scanner.scan(lastTime);
					}
					if (lastTime == 0) {
						ConfigLoader.load();

						// 如果有配置文件，则从配置文件中读取信息并替换注解中的信息
						// 读取配置文件中的方法别名
						List<XMLElement> nds = ConfigLoader.getElements("*.mapping.method");
						for (int i = 0; i < nds.size(); i++) {
							XMLElement data = nds.get(i);
							String id = data.getAttributes().get("id");
							String value = data.getAttributes().get(VALUE);
							AliasMapping.put(id, value);
						}
					}
					lastTime = System.currentTimeMillis();
				}
			} finally {
				lock.unlock();
			}
		}
	}

	@Override
	public void visitClass(BuiltResource br, ClassNode cn) {
		if ((cn.access & Opcodes.ACC_ABSTRACT) != 0) {
			return;// 不能是虚拟类
		}
		if (cn.superName.equals(UIFACADE)) {
			String classAlias = (String) AsmUtil.getAnnotationValue(cn, ALIAS, VALUE);
			for (int i = 0; i < cn.methods.size(); i++) {
				MethodNode mn = cn.methods.get(i);

				if (mn.name == null || mn.name.startsWith("<")) {
					continue;
				}
				if ((mn.access & Opcodes.ACC_PUBLIC) == 0) {// 必须是public方法
					continue;
				}
				if (!AsmUtil.isAnnotationPresent(mn, PRIV)) {// 未用@Priv标明的方法不允许外部访问
					continue;
				}
				boolean flag = AsmUtil.isAnnotationPresent(mn, ALIAS);
				if (classAlias != null && !flag) {// 说明方法没有别名但类有别名
					AliasMapping.put(classAlias + "." + mn.name, cn.name + "#" + mn.name);
				}
				if (flag) {
					String methodAlias = (String) AsmUtil.getAnnotationValue(mn, ALIAS, VALUE);
					Boolean alone = (Boolean) AsmUtil.getAnnotationValue(mn, ALIAS, ALONE);
					if ((alone == null || !alone) && classAlias != null) {
						methodAlias = classAlias + "." + methodAlias;
					}
					AliasMapping.put(methodAlias, cn.name + "#" + mn.name);
				}
			}
		} else if (cn.superName.equals(UIMETHOD)) {
			if (AsmUtil.isAnnotationPresent(cn, ALIAS) && AsmUtil.isAnnotationPresent(cn, PRIV)) {
				AliasMapping.put(AsmUtil.getAnnotationValue(cn, ALIAS, VALUE).toString(), cn.name);
			}
		}
	}

	@Override
	public void visitResource(BuiltResource br) {

	}

	@Override
	public void visitInnerClass(BuiltResource br, ClassNode cn, ClassNode icn) {
		if ((icn.access & Opcodes.ACC_PUBLIC) == 0) {
			return;
		}
		if (!icn.name.startsWith(cn.name)) {
			return;
		}
		if (!AsmUtil.isAnnotationPresent(icn, PRIV)) {// 未用@Priv标明的方法不允许外部访问
			return;
		}
		if (!icn.superName.equals(UIMETHOD)) {
			return;
		}
		boolean flag = AsmUtil.isAnnotationPresent(icn, ALIAS);
		String classAlias = (String) AsmUtil.getAnnotationValue(cn, ALIAS, VALUE);
		if (classAlias != null && !flag) {// 说明方法没有别名但类有别名
			String name = icn.name;
			int index = name.indexOf('/');
			if (index > 0) {
				name = name.substring(index + 1);
			}
			AliasMapping.put(classAlias + "." + name, icn.name);
		}
		if (flag) {
			String innerAlias = (String) AsmUtil.getAnnotationValue(icn, ALIAS, VALUE);
			Boolean innerAlone = (Boolean) AsmUtil.getAnnotationValue(icn, ALIAS, ALONE);
			if ((innerAlone == null || !innerAlone) && classAlias != null) {
				innerAlias = classAlias + "." + innerAlias;
			}
			AliasMapping.put(innerAlias, icn.name);
		}
	}

}
