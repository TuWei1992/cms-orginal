package com.zving.platform.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExpiringSet<E> extends AbstractSet<E> {

	/**
	 * 默认存活时间5分钟
	 */
	public static final int DEFAULT_TIME_TO_LIVE = 60 * 5;

	/**
	 * 默认每1秒检查一次
	 */
	public static final int DEFAULT_EXPIRATION_INTERVAL = 1;

	private static volatile int expirerCount = 1;

	private final ConcurrentHashMap<E, ExpiringObject> m;

	private final Expirer expirer;

	/**
	 * 使用默认值创建一个ExpiringSet实例
	 * DEFAULT_TIME_TO_LIVE and DEFAULT_EXPIRATION_INTERVAL
	 */
	public ExpiringSet() {
		this(DEFAULT_TIME_TO_LIVE, DEFAULT_EXPIRATION_INTERVAL);
	}

	/**
	 * 创建一个ExpiringSet实例
	 * time-to-live value and the default value for DEFAULT_EXPIRATION_INTERVAL
	 * 
	 * @param timeToLive
	 *            The time-to-live value (seconds)
	 */
	public ExpiringSet(int timeToLive) {
		this(timeToLive, DEFAULT_EXPIRATION_INTERVAL);
	}

	/**
	 * 创建一个ExpiringSet实例
	 * a {@link ConcurrentHashMap} for the internal data structure.
	 * 
	 * @param timeToLive
	 *            The time-to-live value (seconds)
	 * @param expirationInterval
	 *            The time between checks to see if a value should be removed (seconds)
	 */
	public ExpiringSet(int timeToLive, int expirationInterval) {
		this(new ConcurrentHashMap<E, ExpiringObject>(), timeToLive, expirationInterval);
	}

	private ExpiringSet(ConcurrentHashMap<E, ExpiringObject> delegate, int timeToLive, int expirationInterval) {
		this.m = delegate;
		this.expirer = new Expirer();
		expirer.setTimeToLive(timeToLive);
		expirer.setExpirationInterval(expirationInterval);
	}

	/**
	 * 获取期限
	 * 
	 * @return
	 */
	public Expirer getExpirer() {
		return expirer;
	}

	/**
	 * 获取期限延迟
	 * 
	 * @return
	 */
	public int getExpirationInterval() {
		return expirer.getExpirationInterval();
	}

	public int getTimeToLive() {
		return expirer.getTimeToLive();
	}

	public void setExpirationInterval(int expirationInterval) {
		expirer.setExpirationInterval(expirationInterval);
	}

	public void setTimeToLive(int timeToLive) {
		expirer.setTimeToLive(timeToLive);
	}

	public int size() {
		return m.size();
	}

	public boolean isEmpty() {
		return m.isEmpty();
	}

	public boolean contains(Object o) {
		ExpiringObject object = m.get(o);
		if (object != null) {
			object.setLastAccessTime(System.currentTimeMillis());
			return true;
		}
		return false;
	}

	public boolean add(E e) {
		if (e == null) {
			return false;
		}
		return m.put(e, new ExpiringObject(e, System.currentTimeMillis())) == null;
	}

	public boolean remove(Object o) {
		return m.remove(o) != null;
	}

	public void clear() {
		m.clear();
	}

	public Iterator<E> iterator() {
		return m.keySet().iterator();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (Iterator<?> i = c.iterator(); i.hasNext();)
			if (remove(i.next()))
				modified = true;
		return modified;
	}

	private class ExpiringObject {

		private E value;

		private long lastAccessTime;

		private final ReadWriteLock lastAccessTimeLock = new ReentrantReadWriteLock();

		ExpiringObject(E value, long lastAccessTime) {
			if (value == null) {
				throw new IllegalArgumentException("An expiring object cannot be null.");
			}
			this.value = value;
			this.lastAccessTime = lastAccessTime;
		}

		public long getLastAccessTime() {
			lastAccessTimeLock.readLock().lock();
			try {
				return lastAccessTime;
			} finally {
				lastAccessTimeLock.readLock().unlock();
			}
		}

		public void setLastAccessTime(long lastAccessTime) {
			lastAccessTimeLock.writeLock().lock();
			try {
				this.lastAccessTime = lastAccessTime;
			} finally {
				lastAccessTimeLock.writeLock().unlock();
			}
		}

		public E getValue() {
			return value;
		}

		@Override
		public boolean equals(Object obj) {
			return value.equals(obj);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}

	/**
	 * 监视期限线程
	 */
	public class Expirer implements Runnable {
		private final ReadWriteLock stateLock = new ReentrantReadWriteLock();

		private long timeToLiveMillis;

		private long expirationIntervalMillis;

		private boolean running = false;

		private final Thread expirerThread;

		/**
		 * 创建一个新的期限.
		 */
		public Expirer() {
			expirerThread = new Thread(this, "ExpiringMapExpirer-" + expirerCount++);
			expirerThread.setDaemon(true);
		}

		public void run() {
			while (running) {
				processExpires();
				try {
					Thread.sleep(expirationIntervalMillis);
				} catch (InterruptedException e) {
				}
			}
		}

		private void processExpires() {
			long timeNow = System.currentTimeMillis();
			for (ExpiringObject o : m.values()) {
				if (timeToLiveMillis <= 0) {
					continue;
				}
				long timeIdle = timeNow - o.getLastAccessTime();
				if (timeIdle >= timeToLiveMillis) {
					m.remove(o.getValue());
				}
			}
		}

		/**
		 * 启动监视线程.
		 */
		public void startExpiring() {
			stateLock.writeLock().lock();
			try {
				if (!running) {
					running = true;
					expirerThread.start();
				}
			} finally {
				stateLock.writeLock().unlock();
			}
		}

		/**
		 * 如果一个线程没有开始，则启动它，否则立刻返回
		 */
		public void startExpiringIfNotStarted() {
			stateLock.readLock().lock();
			try {
				if (running) {
					return;
				}
			} finally {
				stateLock.readLock().unlock();
			}

			stateLock.writeLock().lock();
			try {
				if (!running) {
					running = true;
					expirerThread.start();
				}
			} finally {
				stateLock.writeLock().unlock();
			}
		}

		/**
		 * 根据监视器信息停止进程
		 */
		public void stopExpiring() {
			stateLock.writeLock().lock();
			try {
				if (running) {
					running = false;
					expirerThread.interrupt();
				}
			} finally {
				stateLock.writeLock().unlock();
			}
		}

		/**
		 * 检查监视线程是否正在运行
		 * 
		 * @return
		 *         如果运行则返回true. 否则 false.
		 */
		public boolean isRunning() {
			stateLock.readLock().lock();
			try {
				return running;
			} finally {
				stateLock.readLock().unlock();
			}
		}

		/**
		 * 返回存活时间.
		 * 
		 * @return
		 *         存活时间 (秒)
		 */
		public int getTimeToLive() {
			stateLock.readLock().lock();
			try {
				return (int) timeToLiveMillis / 1000;
			} finally {
				stateLock.readLock().unlock();
			}
		}

		/**
		 * 更新存活时间
		 * 
		 * @param timeToLive
		 *            存活时间 (秒)
		 */
		public void setTimeToLive(long timeToLive) {
			stateLock.writeLock().lock();
			try {
				this.timeToLiveMillis = timeToLive * 1000;
			} finally {
				stateLock.writeLock().unlock();
			}
		}

		/**
		 * 返回检查周期
		 * 
		 * @return
		 *         单位（秒）.
		 */
		public int getExpirationInterval() {
			stateLock.readLock().lock();
			try {
				return (int) expirationIntervalMillis / 1000;
			} finally {
				stateLock.readLock().unlock();
			}
		}

		/**
		 * 设置检查周期
		 * 
		 * @param expirationInterval
		 *            单位（秒）.
		 */
		public void setExpirationInterval(long expirationInterval) {
			stateLock.writeLock().lock();
			try {
				this.expirationIntervalMillis = expirationInterval * 1000;
			} finally {
				stateLock.writeLock().unlock();
			}
		}
	}

}
