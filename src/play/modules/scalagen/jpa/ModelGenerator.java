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
package play.modules.scalagen.jpa;

import java.util.Map;

import org.jvnet.inflector.Noun;

import play.modules.scalagen.TypeRegistry;
import play.modules.scalagen.util.TemplatesHelper;

/**
 * <p>
 * Generator for JPA based model classes and associated unit tests using the
 * attributes supplied on command line.
 * </p>
 * 
 * @author Aishwarya Singhal
 */
public class ModelGenerator {
	private static final String ATTRIBUTE_TEMPLATE = "var ${attributeName}: ${attributeType} = ${attributeDefaultValue}";
	private static final String DEPENDENCY_TEMPLATE = "var ${attributeName} = null";

	/**
	 * 
	 * @param entityName
	 * @param attributes
	 */
	public static void generate(String entityName,
			Map<String, String> attributes, String scheme) {
		String template = TemplatesHelper.getTemplate(scheme + "/model");

		String entityVarName = Character.toLowerCase(entityName.charAt(0))
				+ entityName.substring(1);

		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);
		template = template
				.replace("${TableName}", getTableName(entityVarName));

		template = buildAttributes(template, entityVarName, attributes, scheme);

		TemplatesHelper.flush("app", "models", entityName + ".scala", template);

		generateTests(entityName, entityVarName, attributes, scheme);

		generateYML(entityName, entityVarName, attributes);

		if ("siena".equals(scheme)) {
			generateQueryTrait();
		}
	}

	private static void generateQueryTrait() {
		if (!TemplatesHelper.exists("app", "siena", "QueryOn.scala")) {
			String template = TemplatesHelper.getTemplate("siena/QueryOn");
			TemplatesHelper.flush("app", "siena", "QueryOn.scala", template);
		} else {
			System.out
					.println("* app/siena/QueryOn.scala already exists. Skipping.");
		}
	}

	/**
	 * Generates a table name for the entity.
	 * 
	 * @param entityVarName
	 * @return
	 */
	private static String getTableName(String entityVarName) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < entityVarName.length(); i++) {
			char c = entityVarName.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append("_").append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}

		String name = sb.toString();
		String[] parts = name.split("_");
		String last = parts[parts.length - 1];

		if (!last.endsWith("s")) {
			parts[parts.length - 1] = Noun.pluralOf(last);
		}

		name = "";

		for (int i = 0; i < parts.length; i++) {
			if (!name.trim().equals("")) {
				name += "_";
			}
			name += parts[i];
		}
		return name;
	}

	private static void generateTests(String entityName, String entityVarName,
			Map<String, String> attributes, String scheme) {
		String template = TemplatesHelper.getTemplate(scheme + "/modelTest");

		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		template = buildTestAttributes(template, entityName, attributes, scheme);

		TemplatesHelper.flush("test", "tests", entityName + ".scala", template);
	}

	private static String buildAttributes(String template,
			String entityVarName, Map<String, String> attributes, String scheme) {
		StringBuilder varDefinitions = new StringBuilder();
		StringBuilder constructorParams = new StringBuilder();
		StringBuilder entityAttributesAssignment = new StringBuilder();

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();
			String varType = attribute.getValue();

			if (varName.toLowerCase().equals("id")) {
				continue;
			}

			String defaultVal = TypeRegistry.getDefaultValue(varType);
			varType = TypeRegistry.getTypeName(varType);

			// build the attribute definition
			String var = ATTRIBUTE_TEMPLATE
					.replace("${attributeName}", varName);
			var = var.replace("${attributeType}", varType);
			var = var.replace("${attributeDefaultValue}", defaultVal);

			if (!TypeRegistry.isRegistered(varType) && "jpa".equals(scheme)) {
				var = "@ManyToOne\n  " + var;
			}

			varDefinitions.append(var).append("\n  ");

			// build the constructor parameter
			if (constructorParams.length() != 0) {
				constructorParams.append(", ");
			}
			constructorParams.append(varName + ": " + varType);

			// build the assignment statement
			entityAttributesAssignment.append(entityVarName).append(".")
					.append(varName).append(" = ").append(varName)
					.append("\n    ");
		}

		template = template.replace("${EntityAttributes}",
				varDefinitions.toString());
		template = template.replace("${ConstructorParams}",
				constructorParams.toString());
		template = template.replace("${EntityAttributesAssignment}",
				entityAttributesAssignment.toString());

		return template;
	}

	private static String buildTestAttributes(String template,
			String entityName, Map<String, String> attributes, String scheme) {
		StringBuilder DependentObjects = new StringBuilder();
		StringBuilder assertions = new StringBuilder();
		StringBuilder TestDataValues = new StringBuilder();

		boolean pending = true;

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();
			String varType = attribute.getValue();

			if (varName.toLowerCase().equals("id")) {
				continue;
			}

			String testDataVal = TypeRegistry.getTestDataValue(varType);
			varType = TypeRegistry.getTypeName(varType);

			String var = "";
			boolean dependency = false;
			if (!TypeRegistry.isRegistered(varType)) {
				dependency = true;
				// initialize the dependency
				var = DEPENDENCY_TEMPLATE.replace("${attributeName}", varName);
				var = var.replace("${attributeType}", varType);
				DependentObjects.append(var).append("\n  ");

				testDataVal = varName;
			}

			// build the test data
			if (TestDataValues.length() != 0) {
				TestDataValues.append(", ");
			}
			TestDataValues.append(testDataVal);

			// build the assertion
			if (dependency) {
				// assertions.append("(first").append(entityName).append(".")
				// .append(varName).append(") should not be (null)\n    ");
			} else {
				if ("Date".equals(varType)) {
					assertions.append("(first").append(entityName).append(".")
							.append(varName)
							.append(") should not be (null)\n    ");
				} else {
					assertions.append("(first").append(entityName).append(".")
							.append(varName).append(") should be (")
							.append(testDataVal).append(")\n    ");
				}

				// if the fetch query has not been defined yet, define now.
				if (pending && !testDataVal.startsWith("new ")) {
					String query = "jpa".equals(scheme) ? Character
							.toUpperCase(varName.charAt(0))
							+ varName.substring(1) : varName;
					template = template.replace("${attributeName}", query);
					template = template.replace("${attributeValue}",
							testDataVal);
					pending = false;
				}
			}
		}

		template = template.replace("${DependentObjects}",
				DependentObjects.toString());
		template = template.replace("${TestDataValues}",
				TestDataValues.toString());
		template = template.replace("${assertions}", assertions.toString());

		return template;
	}

	private static void generateYML(String entityName, String entityVarName,
			Map<String, String> attributes) {

		StringBuilder yml = new StringBuilder();
		yml.append("# This is test data for models.").append(entityName)
				.append("\n\n\n");
		yml.append("# ").append(entityName).append("(").append(entityVarName)
				.append("1):\n");

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();
			String varType = attribute.getValue();

			yml.append("#     ").append(varName).append(": ");

			if (TypeRegistry.isRegistered(varType)) {
				yml.append(
						TypeRegistry.getTestDataValue(varType)
								.replace("\"", "")).append("\n");
			} else {
				yml.append(varName).append("1\n");
			}
		}

		TemplatesHelper.flush("", "test", entityVarName + ".yml",
				yml.toString());

	}
}
