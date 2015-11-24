package com.zving.framework.collection;

import java.util.Map;

/**
 * 当key类型为字符串时不区分key的大写小的Map，但通过keySet()遍历key时，key依然保留原始大小写。
 * 
 * @Author 王育春
 * @Date 2009-1-5
 * @Mail wyuch@zving.com
 */
public class CaseIgnoreMapx<K, V> extends Mapx<K, V> {
	private static final long serialVersionUID = 1L;

	public CaseIgnoreMapx() {
		super();
	}

	public CaseIgnoreMapx(boolean threadSafe) {
		super(threadSafe);
	}

	public CaseIgnoreMapx(int initCapacity, boolean threadSafe) {// NO_UCD
		super(initCapacity, threadSafe);
	}

	/**
	 * 根据指定Map创建一个键名不区分大小的Map
	 */
	public CaseIgnoreMapx(Map<? extends K, ? extends V> map) {
		super();
		putAll(map);
	}

	public static int caseIgnoreHash(String str) {
		int h = 0;
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			h = 31 * h + (c > 64 && c < 91 ? c + 32 : c);
		}
		return hash(h);

	}

	/**
	 * 通过覆盖此方法确保大小写字符串取到一样的hash值
	 */
	@Override
	int hash(Object key) {
		if (key instanceof String) {
			return caseIgnoreHash((String) key);
		} else {
			return hash(key.hashCode());
		}
	}

	/**
	 * 覆盖此方法以实现忽略大小写的字符串比较
	 */
	@Override
	boolean eq(Object x, int hash, Entry<K, V> e) {
		if (x == e.key) {
			return true;
		}
		if (x instanceof String) {
			return hash == e.hash;
		}
		return x.equals(e.key);
	}
}
