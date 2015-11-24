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
 * Implementation of the {@link NativeJdbcExtractor} interface for WebLogic, supporting WebLogic Server 8.1 and higher.
 * <p>
 * Returns the underlying native Connection to application code instead of WebLogic's wrapper implementation; unwraps the Connection for
 * native statements. The returned JDBC classes can then safely be cast, e.g. to <code>oracle.jdbc.OracleConnection</code>.
 * <p>
 * This NativeJdbcExtractor can be set just to <i>allow</i> working with a WebLogic DataSource: If a given object is not a WebLogic
 * Connection wrapper, it will be returned as-is.
 * 
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see #getNativeConnection
 * @see weblogic.jdbc.extensions.WLConnection#getVendorConnection
 */
public class WebLogicNativeJdbcExtractor {

	private static final String JDBC_EXTENSION_NAME = "weblogic.jdbc.extensions.WLConnection";

	private static Class<?> jdbcExtensionClass;

	private static Method getVendorConnectionMethod;

	/**
	 * This constructor retrieves the WebLogic JDBC extension interface, so we can get the underlying vendor connection
	 * using reflection.
	 */
	public static void init() {
		try {
			jdbcExtensionClass = WebLogicNativeJdbcExtractor.class.getClassLoader().loadClass(JDBC_EXTENSION_NAME);
			getVendorConnectionMethod = jdbcExtensionClass.getMethod("getVendorConnection", (Class[]) null);
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Could not initialize WebLogicNativeJdbcExtractor because WebLogic API classes are not available: " + ex);
		}
	}

	/**
	 * Retrieve the Connection via WebLogic's <code>getVendorConnection</code> method.
	 */
	public static Connection doGetNativeConnection(Connection con) throws SQLException {
		if (jdbcExtensionClass == null) {
			init();
		}
		if (jdbcExtensionClass.isAssignableFrom(con.getClass())) {
			try {
				return (Connection) getVendorConnectionMethod.invoke(con, new Object[0]);
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
