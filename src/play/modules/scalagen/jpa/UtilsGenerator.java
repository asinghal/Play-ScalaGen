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
		String template = TemplatesHelper.getTemplate("utils/HttpBinder");

		if (!TemplatesHelper.exists("app", "utils", "HttpBinder.scala")) {
			TemplatesHelper.flush("app", "utils", "HttpBinder.scala", template);
			
			template = TemplatesHelper.getTemplate("utils/converterTests");
			TemplatesHelper.flush("test", "tests", "ConverterTests.scala", template);
		} else {
			System.out.println("* app/utils/HttpBinder.scala already exists. Skipping.");
		}
	}
}
