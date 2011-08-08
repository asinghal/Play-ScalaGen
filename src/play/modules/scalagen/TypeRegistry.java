package play.modules.scalagen;

import java.util.HashMap;
import java.util.Map;

public class TypeRegistry {

	private static Map<String, Entry> defaultValues = new HashMap<String, Entry>();

	static {
		defaultValues.put("string", new Entry("String"));
		defaultValues.put("date", new Entry("Date", "new Date", "new Date"));
		defaultValues.put("int", new Entry("Int", "0", "1"));
		defaultValues.put("long", new Entry("Long", "0", "1"));
		defaultValues.put("boolean", new Entry("Boolean", "false", "false"));
		defaultValues.put("double", new Entry("Double", "0.0", "10.0"));
		defaultValues.put("float", new Entry("Float", "0.0", "10.0"));
		defaultValues.put("byte", new Entry("Byte", "0", "1"));
		defaultValues.put("char", new Entry("Char", "''", "'a'"));
		defaultValues.put("short", new Entry("Short", "0", "1"));
	}

	public static boolean isRegistered(String type) {
		return defaultValues.containsKey(type.toLowerCase());
	}

	public static String getDefaultValue(String type) {
		type = type.toLowerCase();

		if (defaultValues.containsKey(type)) {
			return defaultValues.get(type).defaultValue;
		}

		return "null";
	}

	public static String getTypeName(String type) {
		String lowerCaseType = type.toLowerCase();

		if (defaultValues.containsKey(lowerCaseType)) {
			type = defaultValues.get(lowerCaseType).typeName;
		}

		return type;
	}

	public static String getTestDataValue(String type) {
		type = type.toLowerCase();

		if (defaultValues.containsKey(type)) {
			return defaultValues.get(type).testDataValue;
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
