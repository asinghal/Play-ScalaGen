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
 * Generator for Selenium test cases of different CRUD operations. It generates
 * a test suite per model and assumes RESTful URLs.
 * </p>
 * 
 * @author Aishwarya Singhal
 */
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

			formData.append("type('id=").append(varName).append("', '")
					.append(value).append("')\n");
		}
		template = template.replace("${formData}", formData);
		template = template.replace("${editFormData}", editFormData);

		TemplatesHelper.flush("test", "", entityName + ".test.html", template);
	}
}
