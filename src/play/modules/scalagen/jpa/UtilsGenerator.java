package play.modules.scalagen.jpa;

import play.modules.scalagen.util.TemplatesHelper;

/**
 * <p>
 * Generator for utility classes and associated unit tests.
 * </p>
 * 
 * @author Aishwarya Singhal
 */
public class UtilsGenerator {

	/**
	 * Generates utility classes needed for the code to function.
	 */
	public static void generate() {

		// generate the form binder
		if (!TemplatesHelper.exists("app", "utils", "HttpBinder.scala")) {
			String template = TemplatesHelper.getTemplate("utils/HttpBinder");
			TemplatesHelper.flush("app", "utils", "HttpBinder.scala", template);

			template = TemplatesHelper.getTemplate("utils/converterTests");
			TemplatesHelper.flush("test", "tests", "ConverterTests.scala",
					template);
		} else {
			System.out
					.println("* app/utils/HttpBinder.scala already exists. Skipping.");
		}

		// generate the JQ Grid helper
		if (!TemplatesHelper.exists("app", "utils", "JQGridHelper.scala")) {
			String template = TemplatesHelper.getTemplate("utils/JQGridHelper");
			TemplatesHelper.flush("app", "utils", "JQGridHelper.scala",
					template);

			template = TemplatesHelper.getTemplate("utils/jqGridHelperTests");
			TemplatesHelper.flush("test", "tests", "JQGridHelperTests.scala",
					template);
		} else {
			System.out
					.println("* app/utils/JQGridHelper.scala already exists. Skipping.");
		}
	}
}
