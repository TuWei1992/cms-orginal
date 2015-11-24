package com.zving.framework.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.zving.framework.core.FrameworkException;

/**
 * 只读的、线程安全的Map，调用涉及到修改Map的方法时会抛出异常。
 * 本类继承自Mapx只是为了兼容遗留代码中可能的类型转换。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-4-7
 */
public class ReadOnlyMapx<K, V> extends Mapx<K, V> {
	private static final long serialVersionUID = 1L;
	Map<K, V> data;
	transient volatile Set<K> keySet = null;
	transient volatile Collection<V> values = null;
	transient EntrySet entrySet = null;

	public ReadOnlyMapx(Map<K, V> data) {
		if (data == null) {
			throw new NullPointerException();
		}
		this.data = data;
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return data.get(key);
	}

	@Override
	public V put(K key, V value) {
		throw new MapReadOnlyException();
	}

	@Override
	public V remove(Object key) {
		throw new MapReadOnlyException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> t) {
		throw new MapReadOnlyException();
	}

	@Override
	public void clear() {
		throw new MapReadOnlyException();
	}

	@Override
	public Set<K> keySet() {
		return keySet == null ? keySet = new KeySet(data.keySet()) : keySet;
	}

	@Override
	public Collection<V> values() {
		return values == null ? values = new Values(data.values()) : values;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return entrySet == null ? entrySet = new EntrySet(data.entrySet()) : entrySet;
	}

	private class KeySet extends AbstractSet<K> {
		Set<K> data;

		public KeySet(Set<K> data) {
			this.data = data;
		}

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator(data.iterator());
		}

		@Override
		public int size() {
			return data.size();
		}
	}

	private class Values implements Collection<V> {
		Collection<V> data;

		public Values(Collection<V> data) {
			this.data = data;
		}

		@Override
		public Iterator<V> iterator() {
			return new ValueIterator(data.iterator());
		}

		@Override
		public int size() {
			return data.size();
		}

		@Override
		public boolean isEmpty() {
			return data.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return data.contains(o);
		}

		@Override
		public Object[] toArray() {
			return data.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return data.toArray(a);
		}

		@Override
		public boolean add(V o) {
			throw new MapReadOnlyException();
		}

		@Override
		public boolean remove(Object o) {
			throw new MapReadOnlyException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return data.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			throw new MapReadOnlyException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new MapReadOnlyException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new MapReadOnlyException();
		}

		@Override
		public void clear() {
			throw new MapReadOnlyException();

		}
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		Set<Map.Entry<K, V>> data;

		public EntrySet(Set<Map.Entry<K, V>> data) {
			this.data = data;
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator(data.iterator());
		}

		@Override
		public int size() {
			return data.size();
		}
	}

	private class ValueIterator implements Iterator<V> {
		Iterator<V> data;

		public ValueIterator(Iterator<V> data) {
			this.data = data;
		}

		@Override
		public V next() {
			return data.next();
		}

		@Override
		public boolean hasNext() {
			return data.hasNext();
		}

		@Override
		public void remove() {
			throw new MapReadOnlyException();
		}
	}

	private class KeyIterator implements Iterator<K> {
		Iterator<K> data;

		public KeyIterator(Iterator<K> data) {
			this.data = data;
		}

		@Override
		public K next() {
			return data.next();
		}

		@Override
		public boolean hasNext() {
			return data.hasNext();
		}

		@Override
		public void remove() {
			throw new MapReadOnlyException();
		}
	}

	private class EntryIterator implements Iterator<Map.Entry<K, V>> {
		Iterator<Map.Entry<K, V>> data;

		public EntryIterator(Iterator<Map.Entry<K, V>> data) {
			this.data = data;
		}

		@Override
		public Map.Entry<K, V> next() {
			return data.next();
		}

		@Override
		public boolean hasNext() {
			return data.hasNext();
		}

		@Override
		public void remove() {
			throw new MapReadOnlyException();
		}
	}

	public static class MapReadOnlyException extends FrameworkException {
		private static final long serialVersionUID = 1L;

		public MapReadOnlyException() {
			super("Can't modify a ReadOnlyMap!");
		}
	}

}
