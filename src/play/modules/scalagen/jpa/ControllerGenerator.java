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
 * Generator for controller classes and routes. Parses the attributes supplied
 * on command line to generate the various CRUD operations in the controller
 * class. The code generation is based on a controller per persistent model
 * idea.
 * </p>
 * 
 * @author Aishwarya Singhal
 */
public class ControllerGenerator {

	private static final String PARAM_TEMPLATE = "params.get(\"${attributeName}\")";

	private static final String ATTRIBUTE_TEMPLATE = "${EntityNameVar}.${attributeName} = if (!isEmptyString(${param})) ${param}.to${varTypeName} else ${defaultValue}";

	private static final String ATTRIBUTE_RELATIONSHIP_TEMPLATE = "${EntityNameVar}.${attributeName} = if (!isEmptyString(${param})) ${modelName}.findById(${param}.toLong).getOrElse(null) else null";

	/**
	 * 
	 * @param entityName
	 * @param attributes
	 */
	public static void generate(String entityName,
			Map<String, String> attributes, String scheme) {
		String template = TemplatesHelper.getTemplate(scheme + "/controller");

		String entityVarName = Character.toLowerCase(entityName.charAt(0))
				+ entityName.substring(1);

		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		template = buildAttributes(template, entityVarName, attributes);

		TemplatesHelper.flush("app", "controllers", entityName
				+ "sController.scala", template);
		buildRoutes(entityName, entityVarName);
	}

	private static void buildRoutes(String entityName, String entityVarName) {
		String template = TemplatesHelper.getTemplate("jpa/routes");
		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		System.out.println();
		System.out
				.println("Please add the following entries to the routes file");
		System.out.println(template);
		System.out.println();
	}

	/**
	 * 
	 * @param template
	 * @param entityVarName
	 * @param attributes
	 * @return
	 */
	private static String buildAttributes(String template,
			String entityVarName, Map<String, String> attributes) {
		StringBuilder varDefinitions = new StringBuilder();

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();
			String varType = attribute.getValue();

			if (varName.toLowerCase().equals("id")) {
				continue;
			}

			varType = TypeRegistry.getTypeName(varType);
			String defaultValue = TypeRegistry.getDefaultValue(varType);

			// build the attribute definition
			String var = null;
			String param = PARAM_TEMPLATE.replace("${attributeName}", varName);

			if (!TypeRegistry.isRegistered(varType)) {
				// unknown type, is it a custom data type/ another model ?
				var = ATTRIBUTE_RELATIONSHIP_TEMPLATE.replace(
						"${EntityNameVar}", entityVarName);
				var = var.replace("${modelName}", varType);
			} else if (TypeRegistry.isInternalDataType(varType)) {
				var = ATTRIBUTE_TEMPLATE;
				var = var.replace("${EntityNameVar}", entityVarName);
				var = var.replace("${varTypeName}", varType);
			}

			if (null != var) {
				var = var.replace("${attributeName}", varName);
				var = var.replace("${param}", param);
				var = var.replace("${defaultValue}", defaultValue);

				varDefinitions.append(var).append("\n        ");
			}
		}

		template = template.replace("${formData}", varDefinitions.toString());

		return template;
	}
}
