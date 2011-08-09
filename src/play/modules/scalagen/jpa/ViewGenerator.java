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

import play.modules.scalagen.util.TemplatesHelper;

/**
 * <p>
 * Generator for view files. For each model the following files are generated:
 * <ul>
 * <li>index.html.scala : Displays all records for the model.
 * <li>show.html.scala : Displays details of a record.
 * <li>newValue.html.scala : Displays a form to create a new record.
 * <li>edit.html.scala : Displays a form to edit an existing record.
 * <li>form.html.scala : Generates the form for a model.
 * </ul>
 * </p>
 * 
 * @author Aishwarya Singhal
 */
public class ViewGenerator {

	/**
	 * 
	 * @param entityName
	 * @param attributes
	 */
	public static void generate(String entityName,
			Map<String, String> attributes) {

		String entityVarName = Character.toLowerCase(entityName.charAt(0))
				+ entityName.substring(1);

		generateIndex(entityName, entityVarName, attributes);
		generateShow(entityName, entityVarName, attributes);
		generateNew(entityName, entityVarName, attributes);
		generateEdit(entityName, entityVarName, attributes);
		generateForm(entityName, entityVarName, attributes);
	}

	private static void generateIndex(String entityName, String entityVarName,
			Map<String, String> attributes) {
		String indexTemplate = TemplatesHelper.getTemplate("jpa/view_index");

		StringBuilder tableHeaders = new StringBuilder();
		StringBuilder rowValues = new StringBuilder();

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();

			if (varName.toLowerCase().equals("id")) {
				continue;
			}

			tableHeaders.append("    <th>").append(capitalize(varName))
					.append("</th>\n");
			rowValues.append("    <td>@").append(entityVarName).append(".")
					.append(varName).append("</td>\n");
		}

		indexTemplate = indexTemplate.replace("${TableHeaders}",
				tableHeaders.toString());
		indexTemplate = indexTemplate.replace("${RowValues}",
				rowValues.toString());

		indexTemplate = indexTemplate.replace("${EntityName}", entityName);
		indexTemplate = indexTemplate
				.replace("${EntityNameVar}", entityVarName);

		TemplatesHelper.flush("app",
				"views" + System.getProperty("file.separator") + entityName
						+ "s", "index.scala.html", indexTemplate);
	}

	private static void generateShow(String entityName, String entityVarName,
			Map<String, String> attributes) {
		String indexTemplate = TemplatesHelper.getTemplate("jpa/view_show");

		StringBuilder tableHeaders = new StringBuilder();

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();

			if (varName.toLowerCase().equals("id")) {
				continue;
			}

			tableHeaders.append("<br/>    ").append(capitalize(varName))
					.append(":");
			tableHeaders.append(" @").append(entityVarName).append(".")
					.append(varName).append("\n");
		}

		indexTemplate = indexTemplate.replace("${ShowDetails}",
				tableHeaders.toString());

		indexTemplate = indexTemplate.replace("${EntityName}", entityName);
		indexTemplate = indexTemplate
				.replace("${EntityNameVar}", entityVarName);

		TemplatesHelper.flush("app",
				"views" + System.getProperty("file.separator") + entityName
						+ "s", "show.scala.html", indexTemplate);
	}

	private static void generateNew(String entityName, String entityVarName,
			Map<String, String> attributes) {
		String indexTemplate = TemplatesHelper.getTemplate("jpa/view_new");

		indexTemplate = indexTemplate.replace("${EntityName}", entityName);
		indexTemplate = indexTemplate
				.replace("${EntityNameVar}", entityVarName);

		TemplatesHelper.flush("app",
				"views" + System.getProperty("file.separator") + entityName
						+ "s", "newValue.scala.html", indexTemplate);
	}

	private static void generateEdit(String entityName, String entityVarName,
			Map<String, String> attributes) {
		String indexTemplate = TemplatesHelper.getTemplate("jpa/view_edit");

		indexTemplate = indexTemplate.replace("${EntityName}", entityName);
		indexTemplate = indexTemplate
				.replace("${EntityNameVar}", entityVarName);

		TemplatesHelper.flush("app",
				"views" + System.getProperty("file.separator") + entityName
						+ "s", "edit.scala.html", indexTemplate);
	}

	private static void generateForm(String entityName, String entityVarName,
			Map<String, String> attributes) {
		String formTemplate = TemplatesHelper.getTemplate("jpa/view_form");

		StringBuilder formData = new StringBuilder();
		formData.append(
				"<input type=\"hidden\" id=\"id\" name=\"id\" value=\"@_")
				.append(entityVarName).append(".id\" />\n");

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();

			formData.append("<br/>    <label for=\"").append(varName)
					.append("\">").append(capitalize(varName))
					.append("</label>:");
			formData.append("<input type=\"text\" id=\"").append(varName)
					.append("\" name=\"").append(varName)
					.append("\" value=\"@_").append(entityVarName).append(".")
					.append(varName).append("\" />\n");
		}

		formTemplate = formTemplate.replace("${FormInputs}",
				formData.toString());

		formTemplate = formTemplate.replace("${EntityName}", entityName);
		formTemplate = formTemplate.replace("${EntityNameVar}", entityVarName);

		TemplatesHelper.flush("app",
				"views" + System.getProperty("file.separator") + entityName
						+ "s", "form.scala.html", formTemplate);
	}

	private static String capitalize(String value) {
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
