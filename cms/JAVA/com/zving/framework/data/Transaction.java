package com.zving.framework.data;

import java.sql.SQLException;
import java.util.ArrayList;

import com.zving.framework.collection.Executor;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.orm.DAO;
import com.zving.framework.orm.DAOSet;

/**
 * 非阻塞事务处理类，使用本类处理事务不会一开始即占用连接，只有在最后commit()时<br>
 * 才会最终占用数据库连接，在此之前只是将操作缓存在内存之中，从而节约占用数据库连接的时间。<br>
 * 1、一般情况都要求使用本类处理事务，本类在任何情况下都不需要手工管理连接，并且性能较优。<br>
 * 2、因本类缓存了数据库操作，如果数据库操作涉及到大量数据的插入(例如一次插入100W条数据)，<br>
 * 则可能会导致内存溢出，此种情况下请使用阻塞型事务。<br>
 * 3、在本类的事务处理过程中查询数据库，查询到的值依然是事务未开始之前的值(因为在未commit()<br>
 * 之前，实际上未向数据库提交任何操作)。 <br>
 * 4、如果事务处理的过程中需要从数据库查询值，并且要求查询到的值是本次事务己提交的操作的结果，<br>
 * 则需要使用阻塞型事务处理。 <br>
 * 5、若要使用JDBC原生事务处理，请使用DataAccess类，一般情况下不推荐使用。<br>
 * 
 * @see com.zving.framework.data.BlockingTransaction
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2006-7-12
 */
public class Transaction {
	/**
	 * 插入数据
	 */
	public static final int INSERT = 1;

	/**
	 * 更新数据
	 */
	public static final int UPDATE = 2;

	/**
	 * 删除数据
	 */
	public static final int DELETE = 3;

	/**
	 * 往B表备份数据
	 */
	public static final int BACKUP = 4;

	/**
	 * 删除并且备份数据
	 */
	public static final int DELETE_AND_BACKUP = 5;

	/**
	 * 先删除再插入数据
	 */
	public static final int DELETE_AND_INSERT = 6;

	/**
	 * SQL操作
	 */
	public static final int SQL = 7;

	/**
	 * 是否是外部JDBC连接
	 */
	protected boolean outerConnFlag = false;

	protected DataAccess dataAccess;

	/**
	 * 操作队列
	 */
	protected ArrayList<Object> list = new ArrayList<Object>();

	/**
	 * 备份到B表操作的操作人
	 */
	protected String backupOperator;

	/**
	 * 备份到B表操作的备注
	 */
	protected String backupMemo;

	/**
	 * 如果产生异常，则此变量会有值
	 */
	protected String exceptionMessage;// 异常消息

	/**
	 * 执行器列表
	 */
	protected ArrayList<Executor> executorList = new ArrayList<Executor>(4);

	/**
	 * 在此连接池上执行操作
	 */
	protected String poolName;

	/**
	 * 空构造器
	 */
	public Transaction() {
	}

	/**
	 * 构造器
	 * 
	 * @param poolName 事务中的所有SQL将在此连接池中执行
	 */
	public Transaction(String poolName) {// NO_UCD
		this.poolName = poolName;
	}

	/**
	 * 设置当前事务使用的DataAccess对象
	 */
	public void setDataAccess(DataAccess dAccess) {
		dataAccess = dAccess;
		outerConnFlag = true;
	}

	/**
	 * 增加一个SQL操作
	 */
	/**
	 * @param q 构造器
	 * @return 实例本身
	 */
	public void add(QueryBuilder q) {
		list.add(new Object[] { q, new Integer(Transaction.SQL) });
	}

	/**
	 * 增加一个DAO插入操作
	 */
	public void insert(DAO<?> dao) {
		add(dao, Transaction.INSERT);
	}

	/**
	 * 增加一个DAOSet插入操作
	 */
	public void insert(DAOSet<?> set) {
		add(set, Transaction.INSERT);
	}

	/**
	 * 增加一个DAO更新操作
	 */
	public void update(DAO<?> dao) {
		add(dao, Transaction.UPDATE);
	}

	/**
	 * 增加一个DAOSet更新操作
	 */
	public void update(DAOSet<?> set) {
		add(set, Transaction.UPDATE);
	}

	/**
	 * 增加一个DAO删除操作
	 */
	public void delete(DAO<?> dao) {
		add(dao, Transaction.DELETE);
	}

	/**
	 * 增加一个DAOSet删除操作
	 */
	public void delete(DAOSet<?> set) {
		add(set, Transaction.DELETE);
	}

	/**
	 * 增加一个DAO备份操作
	 */
	public void backup(DAO<?> dao) {
		add(dao, Transaction.BACKUP);
	}

	/**
	 * 增加一个DAOSet备份操作
	 */
	public void backup(DAOSet<?> set) {
		add(set, Transaction.BACKUP);
	}

	/**
	 * 增加一个DAO备份删除操作
	 */
	public void deleteAndBackup(DAO<?> dao) {
		add(dao, Transaction.DELETE_AND_BACKUP);
	}

	/**
	 * 增加一个DAOSet备份删除操作
	 */
	public void deleteAndBackup(DAOSet<?> set) {
		add(set, Transaction.DELETE_AND_BACKUP);
	}

	/**
	 * 增加一个DAO删除并新建操作
	 */
	public void deleteAndInsert(DAO<?> dao) {
		add(dao, Transaction.DELETE_AND_INSERT);
	}

	/**
	 * 增加一个DAOSet删除并新建操作
	 */
	public void deleteAndInsert(DAOSet<?> set) {// NO_UCD
		add(set, Transaction.DELETE_AND_INSERT);
	}

	/**
	 * 提交事务到数据库
	 */
	public boolean commit() {
		return commit(true);
	}

	/**
	 * 提交事务，若setAutoCommitStatus为false并且使用的是外部的DataAccess，则只将SQL提交到DataAccess。
	 */
	public boolean commit(boolean setAutoCommitStatus) {
		if (!outerConnFlag) {
			dataAccess = new DataAccess(DBConnPoolManager.getConnection(poolName));
		}
		boolean NoErrFlag = true;
		try {
			if (!outerConnFlag || setAutoCommitStatus) {
				dataAccess.setAutoCommit(false);
			}
			for (int i = 0; i < list.size(); i++) {
				Object[] arr = (Object[]) list.get(i);
				Object obj = arr[0];
				int type = ((Integer) arr[1]).intValue();
				if (!executeOperation(obj, type)) {
					NoErrFlag = false;
					return false;
				}
			}
			dataAccess.commit();
			list.clear();
		} catch (Exception e) {
			e.printStackTrace();
			exceptionMessage = e.getMessage();
			NoErrFlag = false;
			return false;
		} finally {
			if (!NoErrFlag) {
				try {
					dataAccess.rollback();
				} catch (DatabaseException e1) {
					e1.printStackTrace();
				}
			}
			try {
				if (!outerConnFlag || setAutoCommitStatus) {
					dataAccess.setAutoCommit(true);
				}
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
			if (!outerConnFlag) {
				try {
					dataAccess.close();
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < executorList.size(); i++) {
				Executor executor = executorList.get(i);
				executor.execute();
			}
		}
		return true;
	}

	protected boolean executeOperation(Object obj, int type) throws SQLException {
		if (obj instanceof QueryBuilder) {
			dataAccess.executeNoQuery((QueryBuilder) obj);
		} else if (obj instanceof DAO) {
			DAO<?> s = (DAO<?>) obj;
			s.setDataAccess(dataAccess);
			if (type == Transaction.INSERT) {
				if (!s.insert()) {
					return false;
				}
			} else if (type == Transaction.UPDATE) {
				if (!s.update()) {
					return false;
				}
			} else if (type == Transaction.DELETE) {
				s.delete();
			} else if (type == Transaction.BACKUP) {
				if (s.backup(backupOperator, backupMemo) == null) {
					return false;
				}
			} else if (type == Transaction.DELETE_AND_BACKUP) {
				s.deleteAndBackup(backupOperator, backupMemo);
			} else if (type == Transaction.DELETE_AND_INSERT) {
				if (!s.deleteAndInsert()) {
					return false;
				}
			}
		} else if (obj instanceof DAOSet) {
			DAOSet<?> s = (DAOSet<?>) obj;
			s.setDataAccess(dataAccess);
			if (type == Transaction.INSERT) {
				if (!s.insert()) {
					return false;
				}
			} else if (type == Transaction.UPDATE) {
				if (!s.update()) {
					return false;
				}
			} else if (type == Transaction.DELETE) {
				if (!s.delete()) {
					return false;
				}
			} else if (type == Transaction.BACKUP) {
				if (!s.backup(backupOperator, backupMemo)) {
					return false;
				}
			} else if (type == Transaction.DELETE_AND_BACKUP) {
				if (!s.deleteAndBackup(backupOperator, backupMemo)) {
					return false;
				}
			} else if (type == Transaction.DELETE_AND_INSERT) {
				if (!s.deleteAndInsert()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 清除所有的操作
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * 获取执行过程中的SQL异常消息
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	/**
	 * 获取本次事务统一的备份备注信息
	 */
	public String getBackupMemo() {
		return backupMemo;
	}

	/**
	 * 设置本次事务统一的备份备注信息
	 */
	public Transaction setBackupMemo(String backupMemo) {
		this.backupMemo = backupMemo;
		return this;
	}

	/**
	 * 获取本次事务统一的备份人信息
	 */
	public String getBackupOperator() {
		return backupOperator;
	}

	/**
	 * 设置本次事务统一的备份人信息
	 */
	public void setBackupOperator(String backupOperator) {
		this.backupOperator = backupOperator;
	}

	/**
	 * 返回包含所有操作的List
	 */
	public ArrayList<Object> getOperateList() {
		return list;
	}

	/**
	 * 增加一个执行器，执行器中的逻辑将在commit()之后执行
	 */
	public void addExecutor(Executor executor) {
		executorList.add(executor);
	}

	/**
	 * 增加一个DAO操作，操作类型为opType
	 */
	public void add(DAO<?> dao, int opType) {
		list.add(new Object[] { dao, new Integer(opType) });
	}

	/**
	 * 增加一个DAOSet操作，操作类型为opType
	 */
	public void add(DAOSet<?> set, int opType) {
		list.add(new Object[] { set, new Integer(opType) });
	}
}
