package com.zving.framework.collection;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 具有最大长度的线程安全的队列，如果加入新对象的时候已经达到了最大长度，则先将最开始的对象移除。<br>
 * 
 * @Author 王育春
 * @Date 2008-11-20
 * @Mail wyuch@zving.com
 */
public class Queuex<T> implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	private Object[] elementData;//队列元素数组

	private int max;//最大容量

	private int pos;//当前位置

	private int size;//当前队列大小

	private ExitEventListener<Object, T> listener;

	private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

	private Lock rlock = rwlock.readLock();

	private Lock wlock = rwlock.writeLock();

	/**
	 * 构造一个指定最大容量的队列
	 * @param max
	 */
	public Queuex(int max) {
		this.max = max;
		elementData = new Object[max];
	}

	/**
	 * 获取队列中指定索引位的元素
	 * @param index 索引位置(如果索引位置超出队列范围则会抛出运行时异常)
	 * @return
	 */
	public T get(int index) {
		if (size <= index) {
			throw new RuntimeException("Index is out of range：" + index);
		}
		rlock.lock();
		try {
			@SuppressWarnings("unchecked")
			T t = (T) elementData[(pos + index) % max];
			return t;
		} finally {
			rlock.unlock();
		}
	}

	/**
	 * 向队列推入指定元素(如果队列已满则移除最开始放入队列的对象)
	 * @param o 要推入的元素
	 * @return 如果有元素从队列中被挤出,则返回该元素,否则返回null
	 */
	public T push(T o) {
		wlock.lock();
		try {
			if (size == max) {
				@SuppressWarnings("unchecked")
				T r = (T) elementData[pos];
				elementData[pos] = o;
				pos = (pos + 1) % max;
				if (listener != null) {
					listener.onExit(null, r);
				}
				return r;
			} else {
				elementData[(pos + size) % max] = o;
				size++;
				return null;
			}
		} finally {
			wlock.unlock();
		}
	}

	/**
	 * 判断对象是否存在队列中
	 * @param v
	 * @return
	 */
	public boolean contains(Object v) {// NO_UCD
		if (v == null) {
			return false;
		}
		rlock.lock();
		try {
			for (Object element : elementData) {
				if (v.equals(element)) {
					return true;
				}
			}
			return false;
		} finally {
			rlock.unlock();
		}
	}

	/**
	 * 从队列中移除指定元素
	 * @param v 要移除的元素
	 * @return 
	 */
	public boolean remove(Object v) {// NO_UCD
		if (v == null) {
			return false;
		}
		rlock.lock();
		try {
			for (int i = 0; i < size; i++) {
				if (v.equals(get(i))) {
					remove(i);
				}
			}
			return true;
		} finally {
			rlock.unlock();
		}
	}

	/**
	 * 移除指定索引位的元素
	 * @param index 索引位置
	 * @return 被移除的元素
	 */
	public T remove(int index) {
		if (size <= index) {
			throw new RuntimeException("Index is out of range：" + index);
		}
		wlock.lock();
		try {
			T r = get(index);
			index = (index + pos) % max;
			Object[] newarr = new Object[max];
			if (pos == 0) {
				System.arraycopy(elementData, 0, newarr, 0, index);
				System.arraycopy(elementData, index + 1, newarr, index, max - index - 1);
			} else {
				if (index >= pos) {
					System.arraycopy(elementData, pos, newarr, 0, index - pos);
					System.arraycopy(elementData, index + 1, newarr, index - pos, max - index - 1);
					System.arraycopy(elementData, 0, newarr, max - pos - 1, pos);
				} else {
					System.arraycopy(elementData, pos, newarr, 0, max - pos);
					System.arraycopy(elementData, 0, newarr, max - pos, index);
					System.arraycopy(elementData, index + 1, newarr, max - pos + index, pos - index);
				}
				pos = 0;
			}
			elementData = newarr;
			size--;
			return r;
		} finally {
			wlock.unlock();
		}
	}

	/**
	 * 清除队列
	 */
	public void clear() {
		wlock.lock();
		try {
			elementData = new Object[max];
			size = 0;
		} finally {
			wlock.unlock();
		}

	}

	/**
	 * 当前队列大小
	 * @return
	 */
	public int size() {
		return size;
	}

	/**
	 * 设置换出事件监听器,当键值队换出调用
	 */
	public void setExitEventListener(ExitEventListener<Object, T> listener) {
		this.listener = listener;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < 20 && i < size; i++) {
			if (i != 0) {
				sb.append(" , ");
			}
			sb.append(get(i));
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * 获取队列最大容量
	 * @return
	 */
	public int getMax() {
		return max;
	}

}
