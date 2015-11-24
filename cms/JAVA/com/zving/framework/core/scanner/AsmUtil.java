package com.zving.framework.core.scanner;

import com.zving.framework.thirdparty.asm.tree.AnnotationNode;
import com.zving.framework.thirdparty.asm.tree.ClassNode;
import com.zving.framework.thirdparty.asm.tree.MethodNode;

/**
 * ASM工具类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-6
 */
public class AsmUtil {

	/**
	 * @param mn 方法节点
	 * @param annotation 注解类名
	 * @return 指定的方法节点下是否有指定的注解
	 */
	public static boolean isAnnotationPresent(MethodNode mn, String annotation) {
		if (mn.visibleAnnotations == null) {
			return false;
		}
		annotation = "L" + annotation + ";";
		for (AnnotationNode an : mn.visibleAnnotations) {
			if (annotation.equals(an.desc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cn 类型节点
	 * @param annotation 注解类名
	 * @return 类型下是否有指定的注解
	 */
	public static boolean isAnnotationPresent(ClassNode cn, String annotation) {
		if (cn.visibleAnnotations == null) {
			return false;
		}
		annotation = "L" + annotation + ";";
		for (AnnotationNode an : cn.visibleAnnotations) {
			if (annotation.equals(an.desc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cn 类型节点
	 * @param annotation 注解类名
	 * @param prop 属性名
	 * @return 类型下的指定注解的指定属性的值
	 */
	public static Object getAnnotationValue(ClassNode cn, String annotation, String prop) {
		if (cn.visibleAnnotations == null) {
			return null;
		}
		annotation = "L" + annotation + ";";
		for (AnnotationNode an : cn.visibleAnnotations) {
			if (annotation.equals(an.desc)) {
				for (int i = 0; i < an.values.size(); i += 2) {
					if (an.values.get(i).equals(prop)) {
						return an.values.get(i + 1);
					}
				}
				break;
			}
		}
		return null;
	}

	/**
	 * @param mn 方法节点
	 * @param annotation 注解类名
	 * @param prop 属性名
	 * @return 方法下的指定注解的指定属性的值
	 */
	public static Object getAnnotationValue(MethodNode mn, String annotation, String prop) {
		if (mn.visibleAnnotations == null) {
			return null;
		}
		annotation = "L" + annotation + ";";
		for (AnnotationNode an : mn.visibleAnnotations) {
			if (annotation.equals(an.desc)) {
				for (int i = 0; i < an.values.size(); i += 2) {
					if (an.values.get(i).equals(prop)) {
						return an.values.get(i + 1);
					}
				}
				break;
			}
		}
		return null;
	}

}
