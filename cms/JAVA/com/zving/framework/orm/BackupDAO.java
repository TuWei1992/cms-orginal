package com.zving.framework.orm;

import java.util.Date;

import com.zving.framework.Config;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.core.bean.BeanDescription;
import com.zving.framework.core.bean.BeanManager;
import com.zving.framework.core.bean.BeanProperty;
import com.zving.framework.data.exception.DatabaseException;
import com.zving.framework.utility.DateUtil;
import com.zving.framework.utility.ObjectUtil;
import com.zving.framework.utility.StringUtil;

/**
 * 备份表DAO，用于执行备份表上的操作。
 * 
 * @author 王育春
 * @mail wyuch@zving
 * @date 2011-5-17
 */
public class BackupDAO<T extends DAO<T>> extends DAO<BackupDAO<T>> {
	private Class<T> clazz;
	private T dao;
	private String BackupNo;
	private String BackupOperator;
	private Date BackupTime;
	private String BackupMemo;

	public BackupDAO(Class<T> c) {
		try {
			this.clazz = c;
			dao = clazz.newInstance();
			DAOMetadata m = DAOMetadataManager.getMetadata(clazz);
			DAOColumn[] columns = DAOUtil.addBackupColumn(m.getColumns());
			String table = "B" + m.getTable();
			CaseIgnoreMapx<String, BeanProperty> map = m.getBeanProperties();
			BeanDescription bean = BeanManager.getBeanDescription(this.getClass());
			map.put("BackupNo", bean.getProperty("BackupNo"));
			map.put("BackupOperator", bean.getProperty("BackupOperator"));
			map.put("BackupTime", bean.getProperty("BackupTime"));
			map.put("BackupMemo", bean.getProperty("BackupMemo"));
			__metadata = new DAOMetadata(table, columns, m.getIndexes(), map);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public DAOMetadata metadata() {
		return __metadata;
	}

	public DAOMetadata getMetadata() {
		return __metadata;
	}

	public T getDAO() {
		return dao;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BackupDAO<T> newInstance() {
		return new BackupDAO(clazz);
	}

	@Override
	public DAOSet<BackupDAO<T>> newSet() {
		return new DAOSet<BackupDAO<T>>();
	}

	public String getBackupNo() {
		return BackupNo;
	}

	public void setBackupNo(String backupNo) {
		if (backupNo != null) {
			int len = StringUtil.lengthEx(backupNo, Config.getGlobalCharset().equals("UTF-8"));
			if (len > 15) {
				throw new RuntimeException("Data is too long, max is 15,actual is " + len + ":" + backupNo);
			}
		}
		BackupNo = backupNo;
	}

	public String getBackupOperator() {
		return BackupOperator;
	}

	public void setBackupOperator(String backupOperator) {
		if (backupOperator != null) {
			int len = StringUtil.lengthEx(backupOperator, Config.getGlobalCharset().equals("UTF-8"));
			if (len > 50) {
				throw new RuntimeException("Data is too long, max is 50,actual is " + len + ":" + backupOperator);
			}
		}
		BackupOperator = backupOperator;
	}

	public Date getBackupTime() {
		return BackupTime;
	}

	public void setBackupTime(Date backupTime) {
		BackupTime = backupTime;
	}

	public void setBackupTime(String backupTime) {
		if (backupTime == null) {
			this.BackupTime = null;
			return;
		}
		this.BackupTime = DateUtil.parseDateTime(backupTime);
	}

	public String getBackupMemo() {
		return BackupMemo;
	}

	public void setBackupMemo(String backupMemo) {
		if (backupMemo != null) {
			int len = StringUtil.lengthEx(backupMemo, Config.getGlobalCharset().equals("UTF-8"));
			if (len > 200) {
				throw new DatabaseException("Data is too long, max is 200,actual is " + len + ":" + backupMemo);
			}
		}
		BackupMemo = backupMemo;
	}

	/**
	 * 设置名为columnName的字段的值
	 */
	@Override
	public void setV(String columnName, Object v) {
		_init();
		if (ObjectUtil.in(columnName, "BackupNo", "BackupOperator", "BackupMemo", "BackupTime")) {
			__metadata.setV(this, columnName, v);
		} else {
			__metadata.setV(dao, columnName, v);
		}
	}

	/**
	 * 获取名为columnName的字段的值
	 */
	@Override
	public Object getV(String columnName) {
		_init();
		if (ObjectUtil.in(columnName, "BackupNo", "BackupOperator", "BackupMemo", "BackupTime")) {
			return __metadata.getV(this, columnName);
		} else {
			return __metadata.getV(dao, columnName);
		}
	}

	@Override
	public String backup() {
		throw new DAOException("Cann't backup a BackupDAO");
	}

	@Override
	public String backup(String backupOperator, String backupMemo) {
		throw new DAOException("Cann't backup a BackupDAO");
	}

	@Override
	public boolean deleteAndBackup() {
		throw new DAOException("Cann't backup a BackupDAO");
	}

	@Override
	public boolean deleteAndBackup(String backupOperator, String backupMemo) {
		throw new DAOException("Cann't backup a BackupDAO");
	}

}
