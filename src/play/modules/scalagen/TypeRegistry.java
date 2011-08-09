package play.modules.scalagen;

import java.util.HashMap;
import java.util.Map;

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
	}

	public static boolean isRegistered(String type) {
		return registry.containsKey(type.toLowerCase());
	}

	public static String getDefaultValue(String type) {
		type = type.toLowerCase();

		if (registry.containsKey(type)) {
			return registry.get(type).defaultValue;
		}

		return "null";
	}

	public static String getTypeName(String type) {
		String lowerCaseType = type.toLowerCase();

		if (registry.containsKey(lowerCaseType)) {
			type = registry.get(lowerCaseType).typeName;
		}

		return type;
	}

	public static String getTestDataValue(String type) {
		type = type.toLowerCase();

		if (registry.containsKey(type)) {
			return registry.get(type).testDataValue;
		}

		return null;
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
