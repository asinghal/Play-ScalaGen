/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 8th August 2011
 */
package play.modules.scalagen;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a registry of all supported data types.
 * 
 * @author Aishwarya Singhal
 */
public class TypeRegistry {

	private static Map<String, Entry> registry = new HashMap<String, Entry>();

	static {
		registry.put("string", new Entry("String"));
		registry.put("date", new Entry("Date", "new Date", "new Date"));
		registry.put("int", new Entry("Int", "0", "1"));
		registry.put("long", new Entry("Long", "0", "1"));
		registry.put("boolean", new Entry("Boolean", "false", "false"));
		registry.put("double", new Entry("Double", "0.0", "10.0"));
		registry.put("float", new Entry("Float", "0.0", "10.0"));
		registry.put("byte", new Entry("Byte", "0", "1"));
		registry.put("char", new Entry("Char", "''", "'a'"));
		registry.put("short", new Entry("Short", "0", "1"));
		registry.put("bigdecimal", new Entry("BigDecimal", "BigDecimal(0)",
				"BigDecimal(1)"));
		registry.put("bigint", new Entry("BigInt", "BigInt(0)", "BigInt(1)"));
	}

	/**
	 * Checks if a data type is supported or not.
	 * 
	 * @param type
	 * @return true if supported
	 */
	public static boolean isRegistered(String type) {
		return registry.containsKey(type.toLowerCase());
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String getDefaultValue(String type) {
		type = type.toLowerCase();

		if (registry.containsKey(type)) {
			return registry.get(type).defaultValue;
		}

		return "null";
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String getTypeName(String type) {
		String lowerCaseType = type.toLowerCase();

		if (registry.containsKey(lowerCaseType)) {
			type = registry.get(lowerCaseType).typeName;
		}

		return type;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String getTestDataValue(String type) {
		type = type.toLowerCase();

		if (registry.containsKey(type)) {
			return registry.get(type).testDataValue;
		}

		return null;
	}

	/**
	 * Registers an additional data type on the fly.
	 * 
	 * @param typeName
	 * @param defaultValue
	 * @param testDataValue
	 */
	public static void register(String typeName, String defaultValue,
			String testDataValue) {
		if (null != typeName) {
			registry.put(typeName.toLowerCase(), new Entry(typeName,
					defaultValue, testDataValue));
		}
	}
	
	/**
	 * Checks if the type name provided is a Java/ Scala internal data type
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isInternalDataType(String type) {
		return (type.startsWith("java.") || type.startsWith("scala."));
	}

	private static class Entry {
		private String defaultValue;
		private String testDataValue;
		private String typeName;

		private Entry(String typeName, String defaultValue, String testDataValue) {
			this.typeName = typeName;
			this.defaultValue = defaultValue;
			this.testDataValue = testDataValue;
		}

		private Entry(String typeName) {
			this(typeName, "null", "\"test\"");
		}
	}
}
