/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zving.framework.data.nativejdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 移植自Spring：<br>
 * Implementation of the {@link NativeJdbcExtractor} interface for JBoss, supporting JBoss Application Server 3.2.4+.
 * <p>
 * Returns the underlying native Connection, Statement, etc to application code instead of JBoss' wrapper implementations. The returned JDBC
 * classes can then safely be cast, e.g. to <code>oracle.jdbc.OracleConnection</code>.
 * <p>
 * This NativeJdbcExtractor can be set just to <i>allow</i> working with a JBoss connection pool: If a given object is not a JBoss wrapper,
 * it will be returned as-is.
 * 
 * @author Juergen Hoeller
 * @since 03.01.2004
 * @see org.jboss.resource.adapter.jdbc.WrappedConnection#getUnderlyingConnection
 * @see org.jboss.resource.adapter.jdbc.WrappedStatement#getUnderlyingStatement
 * @see org.jboss.resource.adapter.jdbc.WrappedResultSet#getUnderlyingResultSet
 */
public class JBossNativeJdbcExtractor {

	private static final String WRAPPED_CONNECTION_NAME = "org.jboss.resource.adapter.jdbc.WrappedConnection";

	private static Class<?> wrappedConnectionClass;

	private static Method getUnderlyingConnectionMethod;

	/**
	 * This constructor retrieves JBoss JDBC wrapper classes, so we can get the underlying vendor connection using
	 * reflection.
	 */
	public static void init() {
		try {
			wrappedConnectionClass = JBossNativeJdbcExtractor.class.getClassLoader().loadClass(WRAPPED_CONNECTION_NAME);
			getUnderlyingConnectionMethod = wrappedConnectionClass.getMethod("getUnderlyingConnection", (Class[]) null);
		} catch (Exception ex) {
			throw new IllegalStateException("Could not initialize JBossNativeJdbcExtractor because JBoss API classes are not available: "
					+ ex);
		}
	}

	/**
	 * Retrieve the Connection via JBoss' <code>getUnderlyingConnection</code> method.
	 */
	public static Connection doGetNativeConnection(Connection con) throws SQLException {
		if (wrappedConnectionClass == null) {
			init();
		}
		if (wrappedConnectionClass.isAssignableFrom(con.getClass())) {
			try {
				return (Connection) getUnderlyingConnectionMethod.invoke(con, new Object[0]);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return con;
	}
}
