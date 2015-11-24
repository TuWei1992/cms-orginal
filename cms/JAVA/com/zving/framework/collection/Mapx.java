package com.zving.framework.collection;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.zving.framework.data.DataTable;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.Primitives;

/**
 * 改造自LinkedHashMap并增加了一些便利特性的Map：<br>
 * 1、keySet(),values(),entrySet()等方法能够按照键值加入的先后顺序遍历 <br>
 * 2、通过keySet(),values(),entrySet()遍历时总是可以对map进行put,remove等修改操作，而不会引发ConcurrentModificationException异常<br>
 * 3、调用构造器时指定accessOrder=true时，会将最近使用的键值放到前面 <br>
 * 4、调用构造器中时时指定threadSafe=true时，所以操作都将是线程安全的<br>
 * 5、在threadSafe=false时各项操作的性能都和HashMap接近<br>
 * 6、在threadSafe=true时，get(),containsKey()的性能依然和HashMap接近，其余操作最差的情况下也有threadSafe=false时50%的性能<br>
 * 7、提供getXXX系列方法更方便地获取各种常用数据类型 <br>
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-18
 */
@SuppressWarnings("unchecked")
public class Mapx<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {
	static final long serialVersionUID = 201404091412L;
	static final Object NULL_KEY = new Object();
	static final Lock SHAM_LOCK = new ShamLock();
	static final int DEFAULT_INITIAL_CAPACITY = 16;// 默认初始容量
	static final int MAXIMUM_CAPACITY = 1 << 30;
	static final float DEFAULT_LOAD_FACTOR = 0.75f;// 默认加载因子

	transient volatile Set<K> keySet = null;
	transient volatile Collection<V> values = null;
	transient Set<Map.Entry<K, V>> entrySet = null;
	transient KeyNotFoundEventListener<K, V> keyNotFoundEventListener = null;

	transient Entry<K, V>[] table;
	transient int size;
	transient AtomicInteger modCount = new AtomicInteger();// 此变量仅用于entrySet()等方法遍历时的标识是否需要重新计算next值，不需要考虑原子性
	transient Entry<K, V> header;
	transient Lock rlock = SHAM_LOCK;
	transient Lock wlock = SHAM_LOCK;
	transient int threshold;
	transient int accessCount = -1;

	public Mapx() {
		threshold = 12;// (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
		table = new Entry[DEFAULT_INITIAL_CAPACITY];
		init();
	}

	/**
	 * 构造一个指定初始容量的Mapx
	 * 
	 * @param initialCapacity 初始容量
	 */
	public Mapx(int initialCapacity) {// NO_UCD
		this(initialCapacity, false);
	}

	/**
	 * 构造一个指定是否线程安全的Mapx
	 * 
	 * @param threadSafe
	 */
	public Mapx(boolean threadSafe) {
		this();
		if (threadSafe) {
			ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
			rlock = rwlock.readLock();
			wlock = rwlock.writeLock();
		}
	}

	/**
	 * 构造一个映射关系与指定Map相同的Mapx
	 * 
	 * @param m
	 */
	public Mapx(Map<? extends K, ? extends V> m) {// NO_UCD
		this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), false);
		putAllForCreate(m);
	}

	/**
	 * 构造一个指定初始容量和指定线程是否安全的Mapx
	 * 
	 * @param initialCapacity 初始容量
	 * @param threadSafe 是否线程安全
	 */
	public Mapx(int initialCapacity, boolean threadSafe) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		}
		if (initialCapacity > MAXIMUM_CAPACITY) {
			initialCapacity = MAXIMUM_CAPACITY;
		}
		int capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;
		}

		threshold = (int) (capacity * DEFAULT_LOAD_FACTOR);
		table = new Entry[DEFAULT_INITIAL_CAPACITY];
		if (threadSafe) {
			ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
			rlock = rwlock.readLock();
			wlock = rwlock.writeLock();
		}
		init();
	}

	void init() {
		header = new Entry<K, V>(-1, null, null, null);
		header.before = header.after = header;
	}

	<T> T maskKey(T key) {
		return key == null ? (T) NULL_KEY : key;
	}

	static int hash(int h) {
		h += ~(h << 9);
		h ^= h >>> 14;
		h += h << 4;
		h ^= h >>> 10;
		return h;
	}

	int hash(Object key) {
		return hash(key.hashCode());
	}

	boolean eq(Object x, int hash, Entry<K, V> e) {
		return x == e.key || x.equals(e.key);
	}

	static int indexFor(int h, int length) {
		return h & length - 1;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return getEntry(key) != null;
	}

	Entry<K, V> getEntry(Object key) {
		key = maskKey(key);
		int hash = hash(key);
		for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
			if (e.hash == hash && eq(key, hash, e)) {
				return e;
			}
		}
		// 没有找到会进入这里，这通常发生在键值不存在或者table的长度发生变化的时候
		rlock.lock();
		try {
			for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
				if (e.hash == hash && eq(key, hash, e)) {
					return e;
				}
			}
		} finally {
			rlock.unlock();
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		if (key == null) {
			return putForNullKey(value);
		}
		key = maskKey(key);
		int hash = hash(key);
		int i = indexFor(hash, table.length);

		wlock.lock();
		try {
			for (Entry<K, V> e = table[i]; e != null; e = e.next) {
				if (e.hash == hash && eq(key, hash, e)) {
					V oldValue = e.value;
					e.value = value;
					return oldValue;
				}
			}
			modCount.incrementAndGet();
			addEntry(hash, key, value, i);
			return null;
		} finally {
			wlock.unlock();
		}
	}

	V putForNullKey(V value) {
		int hash = hash(NULL_KEY);
		wlock.lock();
		try {
			int i = indexFor(hash, table.length);
			for (Entry<K, V> e = table[i]; e != null; e = e.next) {
				if (e.key == NULL_KEY) {
					V oldValue = e.value;
					e.value = value;
					return oldValue;
				}
			}
			modCount.incrementAndGet();
			addEntry(hash, (K) NULL_KEY, value, i);
			return null;
		} finally {
			wlock.unlock();
		}
	}

	void putForCreate(K key, V value) {
		K k = maskKey(key);
		int hash = hash(k);
		int i = indexFor(hash, table.length);
		for (Entry<K, V> e = table[i]; e != null; e = e.next) {
			if (e.hash == hash && eq(k, hash, e)) {
				e.value = value;
				return;
			}
		}
		createEntry(hash, k, value, i);
	}

	// 都是被线程安全的方法(put,putForNullKey)调用
	void addEntry(int hash, K key, V value, int bucketIndex) {
		createEntry(hash, key, value, bucketIndex);
		Entry<K, V> eldest = header.after;
		if (removeEldestEntry(eldest)) {
			removeEntryForKey(eldest.key);
		} else {
			if (size >= threshold) {
				resize(2 * table.length);
			}
		}
	}

	// 都是被线程安全的方法调用
	void createEntry(int hash, K key, V value, int bucketIndex) {
		Entry<K, V> old = table[bucketIndex];
		Entry<K, V> e = new Entry<K, V>(hash, key, value, old);
		table[bucketIndex] = e;
		e.addBefore(header);
		size++;
	}

	/**
	 * 子类可以覆盖此方法以实现将不需要的Map项移出map的功能。
	 */
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return false;
	}

	/**
	 * 只在创建时调用，threadSafe=true也不需要加锁
	 */
	void putAllForCreate(Map<? extends K, ? extends V> m) {
		for (java.util.Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			putForCreate(e.getKey(), e.getValue());
		}
	}

	/**
	 * 被addEntry,putAll调用，在这些方法中已经加锁了，不需要再次加锁
	 */
	void resize(int newCapacity) {
		Entry<K, V>[] oldTable = table;
		int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return;
		}
		Entry<K, V>[] newTable = new Entry[newCapacity];
		transfer(newTable);
		table = newTable;
		threshold = (int) (newCapacity * DEFAULT_LOAD_FACTOR);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (m == null) {
			return;
		}
		int numKeysToBeAdded = m.size();
		if (numKeysToBeAdded == 0) {
			return;
		}
		wlock.lock();
		try {
			if (numKeysToBeAdded > threshold) {
				int targetCapacity = (int) (numKeysToBeAdded / DEFAULT_LOAD_FACTOR + 1);
				if (targetCapacity > MAXIMUM_CAPACITY) {
					targetCapacity = MAXIMUM_CAPACITY;
				}
				int newCapacity = table.length;
				while (newCapacity < targetCapacity) {
					newCapacity <<= 1;
				}
				if (newCapacity > table.length) {
					resize(newCapacity);
				}
			}
			for (java.util.Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
				put(e.getKey(), e.getValue());
			}
		} finally {
			wlock.unlock();
		}
	}

	@Override
	public V remove(Object key) {
		Entry<K, V> e = removeEntryForKey(key);
		return e == null ? null : e.value;
	}

	Entry<K, V> removeEntryForKey(Object key) {
		Object k = maskKey(key);
		int hash = hash(key);
		wlock.lock();
		try {
			int i = indexFor(hash, table.length);
			Entry<K, V> prev = table[i];
			Entry<K, V> e = prev;
			while (e != null) {
				Entry<K, V> next = e.next;
				if (e.hash == hash && eq(k, hash, e)) {
					modCount.incrementAndGet();
					size--;
					if (prev == e) {
						table[i] = next;
					} else {
						prev.next = next;
					}
					e.recordRemoval(this);
					return e;
				}
				prev = e;
				e = next;
			}
			return e;
		} finally {
			wlock.unlock();
		}
	}

	Entry<K, V> removeMapping(Object o) {
		if (!(o instanceof Map.Entry)) {
			return null;
		}
		Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
		Object k = maskKey(entry.getKey());
		int hash = hash(k);
		wlock.lock();
		try {
			int i = indexFor(hash, table.length);
			Entry<K, V> prev = table[i];
			Entry<K, V> e = prev;
			while (e != null) {
				Entry<K, V> next = e.next;
				if (e.hash == hash && e.equals(entry)) {
					modCount.incrementAndGet();
					size--;
					if (prev == e) {
						table[i] = next;
					} else {
						prev.next = next;
					}
					e.recordRemoval(this);
					return e;
				}
				prev = e;
				e = next;
			}
			return e;
		} finally {
			wlock.unlock();
		}
	}

	/**
	 * 会递归克隆Mapx里面的Mapx
	 */
	@Override
	public Mapx<K, V> clone() {
		Mapx<K, V> result = null;
		try {
			result = (Mapx<K, V>) super.clone();
		} catch (CloneNotSupportedException e) {
		}
		rlock.lock();
		try {
			result.table = new Entry[table.length];
			result.entrySet = null;
			result.modCount.set(0);
			result.size = 0;
			result.init();
			result.putAllForCreate(this);
			for (java.util.Map.Entry<K, V> e : result.entrySet()) {
				if (e != null) {
					V v = e.getValue();
					if (v == null) {
						continue;
					}
					if (v instanceof Mapx) {
						Mapx<?, ?> map = (Mapx<?, ?>) v;
						map = map.clone();
						e.setValue((V) map);
					}
				}
			}
			return result;
		} finally {
			rlock.unlock();
		}
	}

	/**
	 * 获得键的Set，在遍历此Set的同时可以对map进行put,remove等修改操作而不会引发ConcurrentModificationException
	 */
	@Override
	public Set<K> keySet() {
		Set<K> ks = keySet;
		return ks != null ? ks : (keySet = new KeySet());
	}

	/**
	 * 获得值的集合，在遍历此集合的同时可以对map进行put,remove等修改操作而不会引发ConcurrentModificationException
	 */
	@Override
	public Collection<V> values() {
		Collection<V> vs = values;
		return vs != null ? vs : (values = new Values());
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> es = entrySet;
		return es != null ? es : (entrySet = new EntrySet());
	}

	private void writeObject(java.io.ObjectOutputStream s) throws IOException {
		Iterator<Map.Entry<K, V>> i = entrySet().iterator();
		s.defaultWriteObject();
		s.writeInt(table.length);
		s.writeInt(size);
		s.writeInt(accessCount != -1 ? 0 : 1);
		s.writeInt(rlock == SHAM_LOCK ? 0 : 1);
		while (i.hasNext()) {
			Map.Entry<K, V> e = i.next();
			s.writeObject(e.getKey());
			s.writeObject(e.getValue());
		}
	}

	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		int numBuckets = s.readInt();
		table = new Entry[numBuckets];
		threshold = (int) (table.length * DEFAULT_LOAD_FACTOR);
		init();
		int size = s.readInt();
		accessCount = s.readInt();
		int shamFlag = s.readInt();
		modCount = new AtomicInteger();
		if (shamFlag == 0) {
			rlock = wlock = SHAM_LOCK;
		} else {
			ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
			rlock = rwlock.readLock();
			wlock = rwlock.writeLock();
		}
		for (int i = 0; i < size; i++) {
			K key = (K) s.readObject();
			V value = (V) s.readObject();
			putForCreate(key, value);
		}
	}

	void transfer(Entry<K, V>[] newTable) {
		int newCapacity = newTable.length;
		for (Entry<K, V> e = header.after; e != header; e = e.after) {
			int index = indexFor(e.hash, newCapacity);
			e.next = newTable[index];
			newTable[index] = e;
		}
	}

	@Override
	public boolean containsValue(Object value) {
		rlock.lock();
		try {
			if (value == null) {
				for (Entry<K, V> e = header.after; e != header; e = e.after) {
					if (e.value == null) {
						return true;
					}
				}
			} else {
				for (Entry<K, V> e = header.after; e != header; e = e.after) {
					if (value.equals(e.value)) {
						return true;
					}
				}
			}
			return false;
		} finally {
			rlock.unlock();
		}
	}

	@Override
	public V get(Object key) {
		key = maskKey(key);
		Entry<K, V> e = getEntry(key);
		if (e == null) {
			if (this.keyNotFoundEventListener != null && key != NULL_KEY) {
				V v = keyNotFoundEventListener.onKeyNotFound((K) key);
				put((K) key, v);
				return v;
			}
			return null;
		}
		if (accessCount != -1) {
			e.lastAccess = accessCount++;
		}
		return e.value;
	}

	@Override
	public void clear() {
		wlock.lock();
		try {
			modCount.incrementAndGet();
			Entry<K, V>[] tab = table;
			for (int i = 0; i < tab.length; i++) {
				tab[i] = null;
			}
			size = 0;
			header.before = header.after = header;
		} finally {
			wlock.unlock();
		}
	}

	public String getString(K key) {
		Object o = get(key);
		if (o == null) {
			return null;
		} else {
			return o.toString();
		}
	}

	public boolean getBoolean(K key) {
		Object o = get(key);
		return Primitives.getBoolean(o);
	}

	public int getInt(K key) {
		Object o = get(key);
		return Primitives.getInteger(o);
	}

	public long getLong(K key) {
		Object o = get(key);
		return Primitives.getLong(o);
	}

	public float getFloat(K key) {// NO_UCD
		Object o = get(key);
		return Primitives.getFloat(o);
	}

	public double getDouble(K key) {
		Object o = get(key);
		return Primitives.getDouble(o);
	}

	public Date getDate(K key) {
		Object o = get(key);
		if (o instanceof Date) {
			return (Date) o;
		} else if (o != null) {
			try {
				return DateUtil.parseDateTime(o.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 将一个Map转为含有Key,Value两个字段的DataTable
	 */
	public static DataTable toDataTable(Map<?, ?> map) {
		DataTable dt = new DataTable();
		dt.insertColumns(new String[] { "Key", "Value" });
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			dt.insertRow(new Object[] { entry.getKey(), entry.getValue() });
		}
		return dt;
	}

	/**
	 * 将一个Map转为含有Key和Value两个字段的DataTable
	 */
	public DataTable toDataTable() {
		return toDataTable(this);
	}

	Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

	/**
	 * Map项
	 */
	static class Entry<K, V> implements Map.Entry<K, V> {
		Entry<K, V> before, after;
		final K key;
		V value;
		final int hash;
		Entry<K, V> next;
		long lastAccess = 0;

		Entry(int h, K k, V v, Entry<K, V> n) {
			value = v;
			next = n;
			key = k;
			hash = h;
		}

		@Override
		public K getKey() {
			return key == NULL_KEY ? null : key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V newValue) {
			V oldValue = value;
			value = newValue;
			return oldValue;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Entry)) {
				return false;
			}
			Entry<K, V> e = (Entry<K, V>) o;
			Object k1 = getKey();
			Object k2 = e.getKey();
			if (k1 == k2 || k1 != null && k1.equals(k2)) {
				Object v1 = getValue();
				Object v2 = e.getValue();
				if (v1 == v2 || v1 != null && v1.equals(v2)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (key == NULL_KEY ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		@Override
		public String toString() {
			return getKey() + "=" + getValue();
		}

		// 只在removeMapping，removeEntryForKey中调用，这两个方法都已经加过锁了
		void recordRemoval(Mapx<K, V> m) {
			before.after = after;
			after.before = before;
		}

		// 只在createEntry()中调用，不需要加锁
		void addBefore(Entry<K, V> existingEntry) {
			after = existingEntry;
			before = existingEntry.before;
			before.after = this;
			after.before = this;
		}
	}

	class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return newEntryIterator();
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			Entry<K, V> candidate = getEntry(e.getKey());
			return candidate != null && candidate.equals(e);
		}

		@Override
		public boolean remove(Object o) {
			return removeMapping(o) != null;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public void clear() {
			Mapx.this.clear();
		}
	}

	/**
	 * 键Set,threadSafe=false时仍然线程安全
	 */
	class KeySet extends AbstractSet<K> {
		@Override
		public Iterator<K> iterator() {
			return newKeyIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}

		@Override
		public boolean remove(Object o) {
			return Mapx.this.removeEntryForKey(o) != null;
		}

		@Override
		public void clear() {
			Mapx.this.clear();
		}
	}

	/**
	 * 值Set,threadSafe=false时仍然线程安全
	 */
	class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			return newValueIterator();
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean contains(Object o) {
			return containsValue(o);
		}

		@Override
		public void clear() {
			Mapx.this.clear();
		}
	}

	/**
	 * 遍历器虚拟类，对于在遍历中修改map是安全的
	 */
	abstract class AbstractIterator<T> implements Iterator<T> {
		Entry<K, V> nextEntry = header.after;
		Entry<K, V> lastReturned = null;
		int expectedModCount = modCount.get();

		@Override
		public boolean hasNext() {
			return nextEntry != header;
		}

		@Override
		public void remove() {
			if (lastReturned == null) {
				throw new IllegalStateException();
			}
			Mapx.this.remove(lastReturned.key);
			lastReturned = null;
			expectedModCount = modCount.get();
		}

		Entry<K, V> nextEntry() {
			if (nextEntry == header) {
				throw new NoSuchElementException();
			}
			if (modCount.get() != expectedModCount) {
				while (nextEntry != null && !Mapx.this.containsKey(nextEntry.key)) {
					nextEntry = nextEntry.after;
				}
				expectedModCount = modCount.get();
			}
			Entry<K, V> e = lastReturned = nextEntry;
			nextEntry = e.after;
			return e;
		}
	}

	/**
	 * 键遍历器，每次调用keySet().iterator()都会产生新的对象
	 */
	class KeyIterator extends AbstractIterator<K> {
		@Override
		public K next() {
			return nextEntry().getKey();
		}
	}

	/**
	 * 键值遍历器，每次调用values().iterator()都会产生新的对象
	 */
	class ValueIterator extends AbstractIterator<V> {
		@Override
		public V next() {
			return nextEntry().value;
		}
	}

	/**
	 * Map项遍历器，每次调用entrySet().iterator()都会产生新的对象
	 */
	class EntryIterator extends AbstractIterator<Map.Entry<K, V>> {
		@Override
		public Entry<K, V> next() {
			return nextEntry();
		}
	}

	/**
	 * 假锁，在thredSafe=false时使用假锁替代真正的锁，以提高性能
	 */
	static class ShamLock implements Lock {
		@Override
		public void lock() {
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
		}

		@Override
		public boolean tryLock() {
			return true;
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			return true;
		}

		@Override
		public void unlock() {
		}

		@Override
		public Condition newCondition() {
			return null;
		}
	}

	/**
	 * 获取键值未找到事件监听器
	 */
	public KeyNotFoundEventListener<K, V> getKeyNotFoundEventListener() {
		return keyNotFoundEventListener;
	}

	/**
	 * 设置键值未找到事件监听器
	 */
	public void setKeyNotFoundEventListener(KeyNotFoundEventListener<K, V> keyNotFoundEventListener) {
		this.keyNotFoundEventListener = keyNotFoundEventListener;
	}

	/**
	 * 返回Entry数组的长度
	 */
	public int getEntryTableLength() {
		return table.length;
	}
}
