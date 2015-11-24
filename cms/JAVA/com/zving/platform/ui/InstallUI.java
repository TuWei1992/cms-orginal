package com.zving.platform.ui;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;

import com.zving.framework.Config;
import com.zving.framework.UIFacade;
import com.zving.framework.User;
import com.zving.framework.annotation.Alias;
import com.zving.framework.annotation.Priv;
import com.zving.framework.core.handler.ZAction;
import com.zving.framework.data.DBConn;
import com.zving.framework.data.DBConnConfig;
import com.zving.framework.data.DBConnPool;
import com.zving.framework.data.DBConnPoolManager;
import com.zving.framework.data.DataAccess;
import com.zving.framework.data.DataTable;
import com.zving.framework.data.Q;
import com.zving.framework.extend.ExtendManager;
import com.zving.framework.orm.DBImporter;
import com.zving.framework.ui.control.LongTimeTask;
import com.zving.framework.utility.FileUtil;
import com.zving.framework.utility.IOUtil;
import com.zving.framework.utility.LogUtil;
import com.zving.framework.utility.StringUtil;
import com.zving.platform.point.AfterInstallAction;
import com.zving.preloader.Reloader;

/*
 * 初始化数据库
 * 
 * 创建日期 2009-7-7
 * 作者：王育春 
 * 邮箱:wangyc@zving.com
 */
@Alias("Install")
public class InstallUI extends UIFacade {
	@Priv(login = false)
	public void init() {
		if (!Config.isInstalled()) {
			if (Runtime.getRuntime().maxMemory() < 250 * 1024 * 1024) {
				$S("LowMemory", "true");
			}
			$S("NotInstall", "true");
		}
	}

	@Priv(login = false)
	public void execute() {
		if (Config.isInstalled()) {
			fail("已经为" + Config.getAppCode() + "初始数据库完毕，不能再次初始化!");
			return;
		}

		final DBConnConfig dcc = new DBConnConfig();
		dcc.isJNDIPool = $L("isJNDIPool") == 1;
		dcc.isLatin1Charset = $L("isLatin1Charset") == 1;
		dcc.JNDIName = $V("JNDIName");
		dcc.DBName = $V("DBName");
		dcc.DBPassword = $V("Password");
		try {
			dcc.DBPort = Integer.parseInt($V("Port"));
		} catch (NumberFormatException e1) {
			// e1.printStackTrace();
		}
		dcc.DBServerAddress = $V("Address");
		dcc.DBType = $V("ServerType");
		dcc.DBUserName = $V("UserName");

		if (Config.isJboss()) {// JBoss下要去掉前边的jdbc
			if (dcc.JNDIName.toLowerCase().startsWith("jdbc/")) {
				dcc.JNDIName = dcc.JNDIName.substring(5);
			}
		}

		DBConn conn = null;
		try {
			if (dcc.isMysql()) {
				try {
					conn = DBConnPool.createConnection(dcc, false);
				} catch (SQLException e) {// 有可能是因为数据库不存在
					e.printStackTrace();
					dcc.DBName = "mysql";
					try {
						conn = DBConnPool.createConnection(dcc, false);
						// 如果是Mysql,则检查表名大小写
						DataAccess da = new DataAccess(conn);
						DataTable dt = da.executeDataTable(new Q("show variables like 'lower_case_table_names'"));
						if (dt.getRowCount() == 0 || dt.getInt(0, 1) == 0) {
							fail("检查到mysql数据库区分表名大小写，请修改my.cnf或my.ini:<br><font color=red>"
									+ "在[mysqld]段加上一行配置lower_case_table_names=1!</font>");
							conn.closeReally();
							return;
						}
						// 检查字符集
						dt = da.executeDataTable(new Q("show variables like 'character_set_database'"));
						String charset = Config.getGlobalCharset().replaceAll("\\-", "");
						if (!charset.equalsIgnoreCase(dt.getString(0, 1))) {
							fail("检查到mysql的字符集为" + dt.getString(0, 1) + "，但程序要求的字符集为" + charset.toLowerCase()
									+ "，请修改my.cnf或my.ini:<br><font color=red>" + "凡以default-character-set开头的行，都修改为default-character-set="
									+ charset.toLowerCase() + "</font>");
							conn.closeReally();
							return;
						}
						if (!dcc.isJNDIPool) {
							// 检查数据库是否存在，如果不存在，则先创建一个
							dt = da.executeDataTable(new Q("show databases like ?", $V("DBName")));
							if (dt.getRowCount() == 0) {
								LogUtil.info("安装目标数据库不存在，将自动创建目标数据库!");
								da.executeNoQuery(new Q("create schema " + $V("DBName")));
								dcc.DBName = $V("DBName");// 必须改回去,dcc中的值会写入framework.xml
								conn.close();// 必须关闭到mysql的连接
								conn = DBConnPool.createConnection(dcc, false);
							}
						}
					} catch (Exception e2) {// 如果mysql也不能连接，则抛出原异常
						if (conn != null) {
							conn.closeReally();
						}
						throw e;
					}
				} catch (Exception e) {// 除SQLException以外的异常一般是连接没有建立起来
					if (conn != null) {
						try {
							conn.closeReally();
						} catch (Exception e2) {
						}
					}
					throw e;
				}
			} else if (dcc.isSQLServer() || dcc.isSybase()) {
				try {
					conn = DBConnPool.createConnection(dcc, false);
				} catch (SQLException e) {// 有可能是因为数据库不存在
					e.printStackTrace();
					if (dcc.isSQLServer() && !dcc.isJNDIPool) {
						dcc.DBName = "master";
						try {
							conn = DBConnPool.createConnection(dcc, false);
							DataAccess da = new DataAccess(conn);
							// 检查数据库是否存在，如果不存在，则先创建一个
							DataTable dt = da.executeDataTable(new Q("select * from sysDatabases where name=?", $V("DBName")));
							if (dt.getRowCount() == 0) {
								if (DBConnPoolManager.getDBConnConfig().isSQLServer()) {
									LogUtil.info("安装目标数据库不存在，将自动创建目标数据库!");
									da.executeNoQuery(new Q("create database " + $V("DBName")));
									dcc.DBName = $V("DBName");
									conn.closeReally();// 必须关闭到master的连接
									conn = DBConnPool.createConnection(dcc, false);
								}
							} else {// 存在数据库，又不能连接，则说明没有权限
								conn.closeReally();
								fail("用户" + dcc.DBUserName + "没有访问数据库" + $V("DBName") + "的权限！");
								return;
							}
						} catch (Exception e2) {// 如果master也不能连接，则抛出原异常
							throw e;
						}
					} else {
						throw e;
					}
				} catch (Exception e) {// 除SQLException以外的异常一般是连接没有建立起来
					if (conn != null) {
						try {
							conn.closeReally();
						} catch (Exception e2) {
						}
					}
					throw e;
				}
				if (dcc.isSybase() && !dcc.isJNDIPool) {// 要防止数据库不存在直接写入master库的问题
					DataAccess da = new DataAccess(conn);
					try {
						da.executeNoQuery(new Q("use master"));
						// 检查数据库是否存在，如果不存在，则先创建一个
						DataTable dt = da.executeDataTable(new Q("select * from sysdatabases where name=?", $V("DBName")));
						if (dt.getRowCount() == 0) {
							fail("安装目标数据库不存在，请手工创建!<br>" + "注意：<br>1、注意分配给该数据库的存储空间不小于150M！" + "<br>2、服务器页面大小必须为16K"
									+ "<br>3、字符集必须为UTF8且排序规则为nocase！");
							conn.closeReally();
							return;
						}
						da.executeNoQuery(new Q("use " + $V("DBName")));
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
						// 说明没有master库的权限
					}
				}
			} else {
				conn = DBConnPool.createConnection(dcc, false);
			}
			boolean importData = "1".equals($V("ImportData"));
			final DBConn conn2 = conn;
			final boolean autoCreate = "1".equals($V("AutoCreate"));

			if (importData) {
				LongTimeTask ltt = LongTimeTask.getInstanceByType("Install");
				if (ltt != null) {
					fail("相关任务正在运行中，请先中止！");
					return;
				}
				ltt = new LongTimeTask() {
					@Override
					public void execute() {
						try {
							DBImporter di = new DBImporter();
							di.setTask(this);
							Config.setValue("App.DebugMode", "true");
							if (di.importDB(Config.getContextRealPath() + "WEB-INF/data/installer/Install.zdt", conn2, autoCreate, null)) {
								setCurrentInfo("正在初始化系统配置");
								InstallUI.init(conn2);
								setPercent(33);
								generateDatabaseConfig(dcc);
								setCurrentInfo("安装完成，将重定向到登录页面!");
								reload();// 重启应用
							} else {
								addError("<font color=red>导入失败，请查看服务器日志! 确认问题后请按F5刷新页面重新导入。</font>");
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (conn2 != null) {
								try {
									conn2.closeReally();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					}
				};
				ltt.setType("Install");
				ltt.setUser(User.getCurrent());
				ltt.start();
				$S("TaskID", "" + ltt.getTaskID());
				Response.setStatus(1);
			} else {// 只配置连接
				InstallUI.init(conn2);
				generateDatabaseConfig(dcc);
				Response.setStatusAndMessage(2, Config.getAppCode() + "初始化完毕!");
				reload();// 重启应用
			}
		} catch (Exception e) {
			e.printStackTrace();
			Response.setStatusAndMessage(3, "连接到数据库时发生错误:" + e.getMessage());
		}
	}

	public static void reload() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Reloader.isReloading = true;
					Thread.sleep(3000);
					Reloader.reload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.setContextClassLoader(Reloader.class.getClassLoader());
		t.start();

	}

	public static void generateDatabaseConfig(DBConnConfig dcc) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<framework>\n");
		sb.append("	<databases>\n");
		sb.append("		<database name=\"Default\">\n");
		sb.append("			<config name=\"Type\">" + dcc.DBType + "</config>\n");
		if (dcc.isJNDIPool) {
			sb.append("			<config name=\"JNDIName\">" + dcc.JNDIName + "</config>\n");
		} else {
			sb.append("			<config name=\"ServerAddress\">" + dcc.DBServerAddress + "</config>\n");
			sb.append("			<config name=\"Port\">" + dcc.DBPort + "</config>\n");
			sb.append("			<config name=\"Name\">" + dcc.DBName + "</config>\n");
			sb.append("			<config name=\"UserName\">" + dcc.DBUserName + "</config>\n");

			String password = dcc.DBPassword;
			try {
				Class<?> c = Class.forName("com.zving.framework.security.EncryptUtil");
				Method encrypt3DES = c.getMethod("encrypt3DES", new Class<?>[] { String.class, String.class });
				Object defaultKey = c.getField("DEFAULT_KEY").get(null);
				Object obj = encrypt3DES.invoke(null, new Object[] { dcc.DBPassword, defaultKey });
				if (obj != null) {
					password = "$KEY" + obj;
				}
			} catch (Exception e) {
			}
			sb.append("			<config name=\"Password\">" + password + "</config>\n");
			sb.append("			<config name=\"MaxConnCount\">1000</config>\n");
			sb.append("			<config name=\"InitConnCount\">0</config>\n");
			sb.append("			<config name=\"TestTable\">ZDMaxNo</config>\n");
			if (dcc.isLatin1Charset) {
				sb.append("			<config name=\"isLatin1Charset\">true</config>\n");
			}
		}
		sb.append("		</database>\n");
		sb.append("	</databases>\n");
		sb.append("	</framework>\n");
		FileUtil.writeText(Config.getContextRealPath() + "WEB-INF/plugins/classes/database.xml", sb.toString(), "UTF-8");
	}

	@Priv(login = false)
	@Alias(value = "platform/install/sql", alone = true)
	public void getSQL(ZAction za) {

		String dbtype = $V("Type");
		String sql = new DBImporter().getSQL(Config.getContextRealPath() + "WEB-INF/data/installer/Install.zdt", dbtype);
		IOUtil.download(za.getRequest(), za.getResponse(), dbtype + ".txt", new ByteArrayInputStream(sql.getBytes()));
	}

	public static void init(DBConn conn) {
		try {
			if (StringUtil.isNotEmpty(Config.getContextPath())) {
				ExtendManager.invoke(AfterInstallAction.ExtendPointID, new Object[] { conn });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
