package play.modules.scalagen.jpa;

import java.util.Map;

import play.modules.scalagen.TypeRegistry;
import play.modules.scalagen.util.TemplatesHelper;

public class ControllerGenerator {

	private static final String PARAM_TEMPLATE = "params.get(\"${attributeName}\")";

	private static final String ATTRIBUTE_TEMPLATE = "${EntityNameVar}.${attributeName} = if (${param} != null) ${param}.to${varTypeName} else ${defaultValue}";
	private static final String DATE_ATTRIBUTE_TEMPLATE = "${EntityNameVar}.${attributeName} = if (${param} != null) new Date(${param}) else ${defaultValue}";
	private static final String STRING_ATTRIBUTE_TEMPLATE = "${EntityNameVar}.${attributeName} = ${param}";

	private static final String ATTRIBUTE_RELATIONSHIP_TEMPLATE = "${EntityNameVar}.${attributeName} = if (${param} != null) ${modelName}.findById(${param}.toInt).getOrElse(null) else null";

	/**
	 * 
	 * @param entityName
	 * @param attributes
	 */
	public static void generate(String entityName,
			Map<String, String> attributes) {
		String template = TemplatesHelper.getTemplate("jpa/controller");

		String entityVarName = Character.toLowerCase(entityName.charAt(0))
				+ entityName.substring(1);

		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		template = buildAttributes(template, entityVarName, attributes);
		
		TemplatesHelper.flush("app", "controllers", entityName + "sController.scala", template);
		buildRoutes(entityName, entityVarName);
	}

	private static void buildRoutes(String entityName, String entityVarName) {
		String template = TemplatesHelper.getTemplate("jpa/routes");
		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		System.out.println();
		System.out.println("Please add the following entries to the routes file");
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
				var = ATTRIBUTE_RELATIONSHIP_TEMPLATE.replace(
						"${EntityNameVar}", entityVarName);
				var = var.replace("${modelName}", varType);
			} else {
				if ("String".equals(varType)) {
					var = STRING_ATTRIBUTE_TEMPLATE;
				} else if ("Date".equals(varType)) {
					var = DATE_ATTRIBUTE_TEMPLATE;
				} else {
					var = ATTRIBUTE_TEMPLATE;
				}
				var = var.replace("${EntityNameVar}", entityVarName);
				var = var.replace("${varTypeName}", varType);
			}

			var = var.replace("${attributeName}", varName);
			var = var.replace("${param}", param);
			var = var.replace("${defaultValue}", defaultValue);

			varDefinitions.append(var).append("\n        ");
		}

		template = template.replace("${formData}", varDefinitions.toString());

		return template;
	}
}
