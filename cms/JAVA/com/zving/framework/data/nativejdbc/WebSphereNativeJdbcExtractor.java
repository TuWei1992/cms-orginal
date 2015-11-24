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
 * * 移植自Spring：<br>
 * Implementation of the {@link NativeJdbcExtractor} interface for WebSphere, supporting WebSphere Application Server
 * 5.1 and higher.
 * <p>
 * Returns the underlying native Connection to application code instead of WebSphere's wrapper implementation; unwraps the Connection for
 * native statements. The returned JDBC classes can then safely be cast, e.g. to <code>oracle.jdbc.OracleConnection</code>.
 * <p>
 * This NativeJdbcExtractor can be set just to <i>allow</i> working with a WebSphere DataSource: If a given object is not a WebSphere
 * Connection wrapper, it will be returned as-is.
 * 
 * @author Juergen Hoeller
 * @since 1.1
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcConnection
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcUtil#getNativeConnection
 */
public class WebSphereNativeJdbcExtractor {

	private static final String JDBC_ADAPTER_CONNECTION_NAME_5 = "com.ibm.ws.rsadapter.jdbc.WSJdbcConnection";

	private static final String JDBC_ADAPTER_UTIL_NAME_5 = "com.ibm.ws.rsadapter.jdbc.WSJdbcUtil";

	private static Class<?> webSphere5ConnectionClass;

	private static Method webSphere5NativeConnectionMethod;

	/**
	 * This constructor retrieves WebSphere JDBC adapter classes, so we can get the underlying vendor connection using
	 * reflection.
	 */
	public static void init() {
		try {
			webSphere5ConnectionClass = WebSphereNativeJdbcExtractor.class.getClassLoader().loadClass(JDBC_ADAPTER_CONNECTION_NAME_5);
			Class<?> jdbcAdapterUtilClass = WebSphereNativeJdbcExtractor.class.getClassLoader().loadClass(JDBC_ADAPTER_UTIL_NAME_5);
			webSphere5NativeConnectionMethod = jdbcAdapterUtilClass.getMethod("getNativeConnection",
					new Class[] { webSphere5ConnectionClass });
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Could not initialize WebSphereNativeJdbcExtractor because WebSphere API classes are not available: " + ex);
		}
	}

	/**
	 * Retrieve the Connection via WebSphere's <code>getNativeConnection</code> method.
	 */
	public static Connection doGetNativeConnection(Connection con) throws SQLException {
		if (webSphere5ConnectionClass == null) {
			init();
		}
		if (webSphere5ConnectionClass.isAssignableFrom(con.getClass())) {
			try {
				return (Connection) webSphere5NativeConnectionMethod.invoke(null, new Object[] { con });
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
