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
			Map<String, String> attributes) {
		String template = TemplatesHelper.getTemplate("jpa/model");

		String entityVarName = Character.toLowerCase(entityName.charAt(0))
				+ entityName.substring(1);

		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		template = buildAttributes(template, entityVarName, attributes);

		TemplatesHelper.flush("app", "models", entityName + ".scala", template);

		generateTests(entityName, entityVarName, attributes);

		generateYML(entityName, entityVarName, attributes);
	}

	private static void generateTests(String entityName, String entityVarName,
			Map<String, String> attributes) {
		String template = TemplatesHelper.getTemplate("jpa/modelTest");

		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		template = buildTestAttributes(template, entityName, attributes);

		TemplatesHelper.flush("test", "tests", entityName + ".scala", template);
	}

	private static String buildAttributes(String template,
			String entityVarName, Map<String, String> attributes) {
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

			if (!TypeRegistry.isRegistered(varType)) {
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
			String entityName, Map<String, String> attributes) {
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
					String query = Character.toUpperCase(varName.charAt(0))
							+ varName.substring(1);
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
