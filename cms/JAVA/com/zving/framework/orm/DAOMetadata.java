package com.zving.framework.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.zving.framework.annotation.dao.Column;
import com.zving.framework.annotation.dao.Indexes;
import com.zving.framework.annotation.dao.Table;
import com.zving.framework.collection.CaseIgnoreMapx;
import com.zving.framework.core.bean.BeanDescription;
import com.zving.framework.core.bean.BeanManager;
import com.zving.framework.core.bean.BeanProperty;
import com.zving.framework.data.dbtype.IDBType;
import com.zving.framework.utility.ObjectUtil;

/**
 * DAO元数据
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2013-1-8
 */
public class DAOMetadata {
	private String table;
	private String indexes;
	private DAOColumn[] columns;
	private CaseIgnoreMapx<String, BeanProperty> beanProperties;
	private Class<?> clazz;
	private Field[] declaredFields;

	@SuppressWarnings("rawtypes")
	public DAOMetadata(Class<? extends DAO> clazz) {
		this.clazz = clazz;
		declaredFields = clazz.getDeclaredFields();
		for (Field f : declaredFields) {
			f.setAccessible(true);// 有些方法需要访问属性本身
		}
		Table t = getAnnotation(Table.class);
		if (t == null) {
			throw new DAOException("DAO class " + clazz.getName() + " not annotated by @Table!");
		}
		BeanDescription bean = BeanManager.getBeanDescription(clazz);
		table = t.value();
		Indexes i = getAnnotation(Indexes.class);
		if (i != null) {
			indexes = i.value();
		}
		List<DAOColumn> list = new ArrayList<DAOColumn>();
		beanProperties = new CaseIgnoreMapx<String, BeanProperty>();
		for (Field f : getColumnFields()) {
			Column c = f.getAnnotation(Column.class);
			String name = c.name();
			if (ObjectUtil.empty(name)) {
				name = f.getName();
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			beanProperties.put(name, bean.getProperty(name));
			DAOColumn dc = new DAOColumn(name, c.type(), c.length(), c.precision(), c.mandatory(), c.pk());
			list.add(dc);
		}
		columns = new DAOColumn[list.size()];
		columns = list.toArray(columns);
	}

	protected DAOMetadata(String table, DAOColumn[] columns, String indexes, CaseIgnoreMapx<String, BeanProperty> properties) {
		this.table = table;
		this.columns = columns;
		this.indexes = indexes;
		beanProperties = properties;
	}

	private <A extends Annotation> A getAnnotation(Class<A> c) {
		A a = null;
		Class<?> p = clazz;
		while (true) {
			a = p.getAnnotation(c);
			if (a != null) {
				break;
			}
			p = p.getSuperclass();
			if (p == Object.class) {
				break;
			}
		}
		return a;
	}

	private List<Field> getColumnFields() {
		List<Field> list = new ArrayList<Field>();
		Class<?> p = clazz;
		while (true) {
			for (Field f : p.getDeclaredFields()) {
				if (!f.isAnnotationPresent(Column.class)) {
					continue;
				}
				list.add(f);
			}
			p = p.getSuperclass();
			if (p == Object.class) {
				break;
			}
		}
		return list;
	}

	protected Object getV(DAO<?> dao, String column) {
		BeanProperty p = beanProperties.get(column);
		if (p == null) {
			throw new DAOException("DAO class hasn't getter for " + column);
		}
		return p.read(dao);
	}

	protected void setV(DAO<?> dao, String column, Object value) {
		BeanProperty p = beanProperties.get(column);
		if (p == null) {
			throw new DAOException("DAO class hasn't setter for " + column);
		}
		p.write(dao, value);
	}

	protected int getColumnOrder(DAOColumn c) {// NO_UCD
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public String getTable() {
		return table;
	}

	public String getIndexes() {
		return indexes;
	}

	public DAOColumn[] getColumns() {
		return columns;
	}

	public String getInsertSQL(IDBType db) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("insert into ");
		sb.append(table);
		sb.append(" (");
		for (DAOColumn c : columns) {
			if (!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(db.maskColumnName(c.getColumnName()));
		}
		sb.append(") values (");
		for (int i = 0; i < columns.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append("?");
		}
		sb.append(")");
		return sb.toString();
	}

	public String getDeleteSQL(IDBType db) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("delete from ");
		sb.append(table);
		sb.append(" where ");
		first = true;
		for (DAOColumn c : columns) {
			if (!c.isPrimaryKey()) {
				continue;
			}
			if (!first) {
				sb.append(" and ");
			} else {
				first = false;
			}
			sb.append(db.maskColumnName(c.getColumnName()));
			sb.append("=?");
		}
		return sb.toString();
	}

	public String getFillAllSQL(IDBType db) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("select * from ");
		sb.append(table);
		sb.append(" where ");
		first = true;
		for (DAOColumn c : columns) {
			if (!c.isPrimaryKey()) {
				continue;
			}
			if (!first) {
				sb.append(" and ");
			} else {
				first = false;
			}
			sb.append(db.maskColumnName(c.getColumnName()));
			sb.append("=?");
		}
		return sb.toString();
	}

	public String getPrimaryKeyConditions(IDBType db) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append(" where ");
		first = true;
		for (DAOColumn c : columns) {
			if (!c.isPrimaryKey()) {
				continue;
			}
			if (!first) {
				sb.append(" and ");
			} else {
				first = false;
			}
			sb.append(db.maskColumnName(c.getColumnName()));
			sb.append("=?");
		}
		return sb.toString();
	}

	protected CaseIgnoreMapx<String, BeanProperty> getBeanProperties() {
		return beanProperties;
	}

	public Field[] getDeclaredFields() {
		return declaredFields;
	}

}
