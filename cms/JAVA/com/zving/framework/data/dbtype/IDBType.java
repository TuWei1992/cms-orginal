package com.zving.framework.data.dbtype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.QueryBuilder;
import com.zving.framework.data.command.AddColumnCommand;
import com.zving.framework.data.command.AdvanceChangeColumnCommand;
import com.zving.framework.data.command.AlterKeyCommand;
import com.zving.framework.data.command.ChangeColumnLengthCommand;
import com.zving.framework.data.command.ChangeColumnMandatoryCommand;
import com.zving.framework.data.command.CreateIndexCommand;
import com.zving.framework.data.command.CreateTableCommand;
import com.zving.framework.data.command.DropColumnCommand;
import com.zving.framework.data.command.DropIndexCommand;
import com.zving.framework.data.command.DropTableCommand;
import com.zving.framework.data.command.RenameColumnCommand;
import com.zving.framework.data.command.RenameTableCommand;
import com.zving.framework.extend.IExtendItem;

/**
 * 数据库类型接口
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2012-9-12
 */
public interface IDBType extends IExtendItem {

	/**
	 * 是否完全支持此类型的数据库。如果不支持则只能用于外部数据库连接。
	 */
	public boolean isFullSupport();

	/**
	 * @return 数据库类型对应的JDBC驱动类
	 */
	public String getDriverClass();

	/**
	 * @param dcc 数据库连接池配置信息
	 * @return 根据数据库连接池配置信息创建的JDBC连接
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Connection createConnection(DBConnConfig dcc) throws SQLException, ClassNotFoundException;

	/**
	 * @param dcc 数据库配置信息
	 * @return 根据数据库配置信息生成的JDBC URL
	 */
	public String getJdbcUrl(DBConnConfig dcc);

	/**
	 * JDBC连接创建后执行的初始化语句（用于指定字符集、设置连接会话变量等）
	 * 
	 * @param conn 新创建的连接
	 * @throws SQLException
	 */
	public void afterConnectionCreate(DBConn conn) throws SQLException;

	/**
	 * 返回数据库类型的默认端口
	 */
	public int getDefaultPort();

	/**
	 * @param c 创建数据表指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(CreateTableCommand c);

	/**
	 * @param c 添加字段指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(AddColumnCommand c);

	/**
	 * @param c 修改主键指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(AlterKeyCommand c);

	/**
	 * @param c 复杂字段修改指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(AdvanceChangeColumnCommand c);

	/**
	 * @param c 创建索引指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(CreateIndexCommand c);

	/**
	 * @param c 删除字段指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(DropColumnCommand c);

	/**
	 * @param c 删除索引指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(DropIndexCommand c);

	/**
	 * @param c 删除数据表指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(DropTableCommand c);

	/**
	 * @param c 重命名数据表指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(RenameTableCommand c);

	/**
	 * @param c 重命名字段指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(RenameColumnCommand c);

	/**
	 * @param c 修改字段长度指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(ChangeColumnLengthCommand c);

	/**
	 * @param c 修改字段非空属性指令
	 * @return 输出指令在当前数据库类型中对应的SQL
	 */
	public String[] toSQLArray(ChangeColumnMandatoryCommand c);

	/**
	 * @param table 数据表名
	 * @return 当前数据类型下的主键的SQL形式
	 */
	public String getPKNameFragment(String table);

	/**
	 * 将JAVA类型转换为本数据库类型对应的字段类型
	 * 
	 * @param dataType 字段数据类型，见DataTypes类
	 * @param length 字段长度
	 * @param precision 字段精度
	 * @return
	 */
	public String toSQLType(int dataType, int length, int precision);

	/**
	 * 设置PreparedStatement中的Blob变量的值
	 * 
	 * @param conn 数据库连接
	 * @param ps PreparedStatement
	 * @param i 变量序号
	 * @param v 要设置的值
	 * @throws SQLException
	 */
	public void setBlob(DBConn conn, PreparedStatement ps, int i, byte[] v) throws SQLException;

	/**
	 * 设置PreparedStatement中的Clob变量的值
	 * 
	 * @param conn 数据库连接
	 * @param ps PreparedStatement
	 * @param i 变量序号
	 * @param v 要设置的值
	 * @throws SQLException
	 */
	public void setClob(DBConn conn, PreparedStatement ps, int i, Object v) throws SQLException;

	/**
	 * @param conn 数据库连接
	 * @param qb 查询器
	 * @param pageSize 每页条数
	 * @param pageIndex 第几页
	 * @return 分页查询SQL
	 */
	public QueryBuilder getPagedQueryBuilder(DBConn conn, QueryBuilder orginalQ, int pageSize, int pageIndex);

	/**
	 * @return SQL语句分隔符
	 */
	public String getSQLSperator();

	/**
	 * @param message 注释
	 * @return 注释在当前数据库类型中的形式
	 */
	public String getComment(String message);

	/**
	 * @return select时加锁的语句。例如在Oracle下应该返回" for update"
	 */
	public String getForUpdate();

	/**
	 * 从ResultSet中获取当前行的指定列的值
	 * 
	 * @param rs JDBC查询返回的ResultSet
	 * @param columnIndex 列顺序(下标从1开始)
	 * @param dataType 数据类型
	 */
	public Object getValueFromResultSet(ResultSet rs, int columnIndex, int dataType, boolean latin1Flag) throws SQLException;

	/**
	 * 遮掩字段名，当字段名为本数据库专有的关键字时，应该用双引号将字段名包裹
	 */
	public String maskColumnName(String columnName);
}
