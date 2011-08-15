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

import java.util.Calendar;
import java.util.Map;

import play.modules.scalagen.TypeRegistry;
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

		generateDateInput();
		generateIndex(entityName, entityVarName, attributes);
		generateShow(entityName, entityVarName, attributes);
		generateNew(entityName, entityVarName, attributes);
		generateEdit(entityName, entityVarName, attributes);
		generateForm(entityName, entityVarName, attributes);
		generateJQueryJS(entityName, entityVarName, attributes);
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
			String varType = attribute.getValue();
			varType = TypeRegistry.getTypeName(varType);

			formData.append("<br/>    <label for=\"").append(varName)
					.append("\">").append(capitalize(varName))
					.append("</label>:");
			if (!varType.equals("Date")
					&& !varType.equals(Calendar.class.getName())) {
				formData.append("<input type=\"text\" id=\"").append(varName)
						.append("\" name=\"").append(varName)
						.append("\" value=\"@_").append(entityVarName)
						.append(".").append(varName).append("\" />\n");
			} else {
				formData.append(getDateElement(varName));
			}

		}

		formTemplate = formTemplate.replace("${FormInputs}",
				formData.toString());

		formTemplate = formTemplate.replace("${EntityName}", entityName);
		formTemplate = formTemplate.replace("${EntityNameVar}", entityVarName);

		TemplatesHelper.flush("app",
				"views" + System.getProperty("file.separator") + entityName
						+ "s", "form.scala.html", formTemplate);
	}

	private static void generateDateInput() {
		if (!TemplatesHelper.exists("app", "views", "dateInput.scala.html")) {
			String indexTemplate = TemplatesHelper
					.getTemplate("jpa/view_dateInput");

			int year = Calendar.getInstance().get(Calendar.YEAR) - 5;
			StringBuilder formData = new StringBuilder();

			for (int i = year; i < year + 11; i++) {
				formData.append("<option value=\"").append(year).append("\">")
						.append(i).append("</option>\n");
			}

			indexTemplate = indexTemplate.replace("${years}",
					formData.toString());
			TemplatesHelper.flush("app", "views", "dateInput.scala.html",
					indexTemplate);
		}
	}

	private static String getDateElement(String varName) {
		StringBuilder formData = new StringBuilder();

		formData.append("@dateInput(_monthElement=\"").append(varName)
				.append("_month\", _dayElement=\"").append(varName)
				.append("_day\", _yearElement=\"").append(varName)
				.append("_year\")\n");

		return formData.toString();
	}

	private static void generateJQueryJS(String entityName,
			String entityVarName, Map<String, String> attributes) {
		String jsTemplate = TemplatesHelper.getTemplate("jpa/grid_js");
		StringBuilder colModel = new StringBuilder();

		final String COL_MODEL_FORMAT = "{ name: '${header}', index: '${attribute}', hidden: false, sortable: true ${formatOptions} }";

		for (Map.Entry<String, String> attribute : attributes.entrySet()) {
			String varName = attribute.getKey();
			String varType = attribute.getValue();
			varType = TypeRegistry.getTypeName(varType);
			String model = COL_MODEL_FORMAT.replace("${attribute}", varName);
			model = model.replace("${header}", capitalize(varName));

			if (varType.equals("Date")
					|| varType.equals(Calendar.class.getName())) {
				String formatoptions = ", formatoptions: { srcformat: 'Y/m/d h:i:s', newformat: 'd-M-Y' }";
				model = model.replace("${formatOptions}", formatoptions);

			} else {
				model = model.replace("${formatOptions}", "");
			}

			if (colModel.length() != 0) {
				colModel.append(", \n");
			}

			colModel.append(model);
		}

		jsTemplate = jsTemplate.replace("${EntityName}", entityName);
		jsTemplate = jsTemplate.replace("${EntityNameVar}", entityVarName);
		jsTemplate = jsTemplate.replace("${ColumnModel}", colModel);

		TemplatesHelper.flush("public", "javascripts", entityVarName + "s.js",
				jsTemplate);
	}

	private static String capitalize(String value) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (i == 0) {
				c = Character.toUpperCase(c);
			} else if (Character.isUpperCase(c)) {
				sb.append(" ");
			}

			sb.append(c);
		}

		return sb.toString().trim();
	}
}
