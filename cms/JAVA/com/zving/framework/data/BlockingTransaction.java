package com.zving.framework.data;

import java.sql.SQLException;

import com.zving.framework.collection.Executor;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.orm.DAO;
import com.zving.framework.orm.DAOSet;

/**
 * 阻塞型事务处理类，使用本类处理事务时会在一开始就占用数据库连接，<br>
 * 并在提交时自动释放连接。<br>
 * 两种情况下必须使用本类处理事务：<br>
 * 1、事务中后续操作需要查询前面操作的结果。<br>
 * 2、事务中涉及到大量数据的操作。<br>
 * 3、事务提交后会自动关闭连接，如果一直未关闭连接，则会在退出Servlet线程时自动关闭。<br>
 * 以一次插入一百万条数据为例，使用非阻塞事务，<br>
 * 即便是分成1万次操作，一次插入100条，也会将100w条数据全部缓存在内存中，<br>
 * 因而需要100万条数据的内存容量；而使用本类时,因每次操作都己提交到数据库，<br>
 * 则始终只需要100条数据的内存容量。<br>
 * <br>
 * 注意：在阻塞式事务中不使用读写分离，所有操作都在主数据库上进行。
 * 
 * @see com.zving.framework.data.Transaction
 * @Author 王育春
 * @Date 2010-4-7
 * @Mail wyuch@zving.com
 */
public class BlockingTransaction extends Transaction {
	/**
	 * 是否还有未决操作，即是否有操作已经发送到数据库但既未提交又未回滚。
	 */
	private boolean isExistsOpeningOperate = false;

	/**
	 * 当前线程中的BlockingTransaction
	 */
	private static ThreadLocal<BlockingTransaction> current = new ThreadLocal<BlockingTransaction>();

	/**
	 * 占用的数据库连接
	 */
	private DBConn conn;

	/**
	 * 以默认数据库中的连接构造实例
	 */
	public BlockingTransaction() {
		conn = DBConnPoolManager.getConnection();
		conn.isBlockingTransactionStarted = true;
		dataAccess = new DataAccess(conn);
		try {
			dataAccess.nonuseRWSpliting();
			dataAccess.setAutoCommit(false);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		bindTransactionToThread();
	}

	/**
	 * 以指定的连接池中的连接构造实例
	 * 
	 * @param poolName 连接池名称
	 */
	public BlockingTransaction(String poolName) {// NO_UCD
		dataAccess = new DataAccess(DBConnPoolManager.getConnection(poolName));
		conn = dataAccess.getConnection();
		conn.isBlockingTransactionStarted = true;
		try {
			dataAccess.nonuseRWSpliting();
			dataAccess.setAutoCommit(false);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		bindTransactionToThread();
	}

	/**
	 * 以指定的DataAccess构造实例
	 * 
	 * @param da DataAccess
	 */
	public BlockingTransaction(DataAccess da) {// NO_UCD
		dataAccess = da;
		conn = dataAccess.getConnection();
		conn.isBlockingTransactionStarted = true;
		try {
			dataAccess.nonuseRWSpliting();
			dataAccess.setAutoCommit(false);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		bindTransactionToThread();
	}

	/**
	 * 立即执行查询器
	 */
	@Override
	public void add(QueryBuilder q) {
		executeWithBlockedConnection(q, Transaction.SQL, true);
	}

	/**
	 * 增加一个DAO操作,并立即将操作发送到数据库
	 */
	@Override
	public void add(DAO<?> dao, int type) {
		executeWithBlockedConnection(dao, type, true);
	}

	/**
	 * 增加一个DAOSet操作,并立即将操作发送到数据库
	 */
	@Override
	public void add(DAOSet<?> set, int type) {
		executeWithBlockedConnection(set, type, true);
	}

	/**
	 * 增加一个SQL操作,并立即将操作发送到数据库
	 */
	public void addWithException(QueryBuilder qb) throws Exception {// NO_UCD
		executeWithBlockedConnection(qb, Transaction.SQL, false);
	}

	/**
	 * 增加一个DAO操作,并立即将操作发送到数据库
	 */
	public void addWithException(DAO<?> dao, int type) throws Exception {// NO_UCD
		executeWithException(dao, type);
	}

	/**
	 * 增加一个DAOSet操作,并立即将操作发送到数据库
	 */
	public void addWithException(DAOSet<?> set, int type) throws Exception {// NO_UCD
		executeWithException(set, type);
	}

	/**
	 * 执行操作并开始占用连接,如果执行有误，则直接回滚
	 */
	private void executeWithBlockedConnection(Object obj, int type, boolean rollBackFlag) {
		try {
			executeOperation(obj, type);
			isExistsOpeningOperate = true;
		} catch (SQLException e) {
			if (rollBackFlag) {
				try {
					dataAccess.rollback();
					conn.isBlockingTransactionStarted = false;
				} catch (DatabaseException e1) {
					e1.printStackTrace();
				} finally {
					try {
						dataAccess.close();
					} catch (DatabaseException e1) {
						e1.printStackTrace();
					}
				}
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * 执行操作并开始占用连接,如果执行有误，则抛出异常
	 */
	private void executeWithException(Object obj, int type) throws Exception {
		executeOperation(obj, type);
		isExistsOpeningOperate = true;
	}

	/**
	 * 提交事务到数据库
	 */
	@Override
	public boolean commit() {
		return commit(true);
	}

	/**
	 * 提交事务，并关闭连接<br>
	 * 若setAutoCommitStatus为false并且使用的是外部的DataAccess，则提交后并不设置连接的AutoCommit状态。
	 */
	@Override
	public boolean commit(boolean setAutoCommitStatus) {
		try {
			dataAccess.commit();
		} catch (DatabaseException e) {
			e.printStackTrace();
			exceptionMessage = e.getMessage();
			try {
				dataAccess.rollback();// 如果有错，则回滚。有可能add()时没有错误，但提交时报错
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
			return false;
		} finally {
			try {
				if (setAutoCommitStatus) {
					dataAccess.setAutoCommit(true);
				}
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
			isExistsOpeningOperate = false;
			current.set(null);// 已经关闭，不能再绑定
		}
		for (int i = 0; i < executorList.size(); i++) {
			Executor executor = executorList.get(i);
			executor.execute();
		}
		return true;
	}

	/**
	 * 回滚事务,并关闭连接
	 */
	public void rollback() {
		try {
			dataAccess.rollback();
			isExistsOpeningOperate = false;
			conn.isBlockingTransactionStarted = false;
			dataAccess.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			current.set(null);// 已经关闭，不能再绑定
		}
	}

	/**
	 * 将事务绑定到当前线程
	 */
	private void bindTransactionToThread() {
		Object obj = current.get();
		if (obj == null) {
			current.set(this);
		} else {
			throw new RuntimeException("One thread cann't have two BlockingTransaction!");
		}
	}

	/**
	 * 清除掉事务与线程的绑定，并检测连接是否己被关闭。<br>
	 * 此方法仅供MainFilter调用。
	 */
	public static void clearTransactionBinding() {
		Object obj = current.get();
		if (obj == null) {
			return;
		}
		BlockingTransaction bt = (BlockingTransaction) obj;
		if (bt.isExistsOpeningOperate) {
			bt.rollback();
		}
		current.set(null);
	}

	/**
	 * 获取当前线程中的阻塞型事务所用的连接。<br>
	 * 此方法主要供DBConnPool.getConnection()调用，<br>
	 * 以保证在阻塞型事务中执行的查询类操作能够获得正确的结果。
	 */
	public static DBConn getCurrentThreadConnection() {
		Object obj = current.get();
		if (obj == null) {
			return null;
		}
		BlockingTransaction bt = (BlockingTransaction) obj;
		if (bt.dataAccess == null || bt.dataAccess.conn == null) {// 注意如果没有dataAccess.conn的检测，则会死循环
			return null;
		}
		return bt.dataAccess.getConnection();
	}

	/**
	 * 关闭事务中的连接
	 */
	public void close() {
		try {
			conn.isBlockingTransactionStarted = false;
			if (!conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			current.set(null);// 已经关闭，不能再绑定
		}
	}

	@Override
	public void setDataAccess(DataAccess dAccess) {
		throw new DatabaseException("Can't setDataAccess() from BlockingTransaction!");
	}

}
