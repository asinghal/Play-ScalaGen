package play.modules.scalagen.jpa;

import java.util.Map;

import play.modules.scalagen.TypeRegistry;
import play.modules.scalagen.util.TemplatesHelper;

public class SeleniumTestGenerator {

	/**
	 * 
	 * @param entityName
	 * @param attributes
	 */
	public static void generate(String entityName,
			Map<String, String> attributes) {
		String template = TemplatesHelper.getTemplate("jpa/seleniumTests");

		String entityVarName = Character.toLowerCase(entityName.charAt(0))
				+ entityName.substring(1);

		template = template.replace("${EntityName}", entityName);
		template = template.replace("${EntityNameVar}", entityVarName);

		StringBuilder formData = new StringBuilder();

		StringBuilder editFormData = new StringBuilder();

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();
			String varType = TypeRegistry.getTypeName(attribute.getValue());

			if (varName.toLowerCase().equals("id")
					|| !TypeRegistry.isRegistered(varType)) {
				continue;
			}

			String value = TypeRegistry.getTestDataValue(varType).replace("\"",
					"");

			if ("Date".equals(varType)) {
				value = "";
				editFormData.append("type('id=").append(varName).append("', '")
						.append(value).append("')\n");
			}

			formData.append("type('id=").append(varName).append("', '")
					.append(value).append("')\n");
		}
		template = template.replace("${formData}", formData);
		template = template.replace("${editFormData}", editFormData);

		TemplatesHelper.flush("test", "", entityName + ".test.html", template);
	}
}
