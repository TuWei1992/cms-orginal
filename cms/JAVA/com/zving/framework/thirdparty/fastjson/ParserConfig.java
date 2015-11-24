/*
 * Copyright 1999-2101 Alibaba Group.
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
package com.zving.framework.thirdparty.fastjson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class ParserConfig {

	public static ParserConfig getGlobalInstance() {
		return global;
	}

	private final Set<Class<?>> primitiveClasses = new HashSet<Class<?>>();

	private static ParserConfig global = new ParserConfig();

	protected final SymbolTable symbolTable = new SymbolTable();

	public ParserConfig() {
		primitiveClasses.add(boolean.class);
		primitiveClasses.add(Boolean.class);

		primitiveClasses.add(char.class);
		primitiveClasses.add(Character.class);

		primitiveClasses.add(byte.class);
		primitiveClasses.add(Byte.class);

		primitiveClasses.add(short.class);
		primitiveClasses.add(Short.class);

		primitiveClasses.add(int.class);
		primitiveClasses.add(Integer.class);

		primitiveClasses.add(long.class);
		primitiveClasses.add(Long.class);

		primitiveClasses.add(float.class);
		primitiveClasses.add(Float.class);

		primitiveClasses.add(double.class);
		primitiveClasses.add(Double.class);

		primitiveClasses.add(BigInteger.class);
		primitiveClasses.add(BigDecimal.class);

		primitiveClasses.add(String.class);
		primitiveClasses.add(java.util.Date.class);
		primitiveClasses.add(java.sql.Date.class);
		primitiveClasses.add(java.sql.Time.class);
		primitiveClasses.add(java.sql.Timestamp.class);
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
}
