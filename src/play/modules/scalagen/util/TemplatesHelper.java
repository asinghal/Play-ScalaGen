package play.modules.scalagen.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TemplatesHelper {

	private static final String BASE_DIR = System
			.getProperty("application.path");
	private static final String SLASH = System.getProperty("file.separator");

	public static String getTemplate(String name) {

		InputStream in = TemplatesHelper.class.getClassLoader()
				.getResourceAsStream("templates/" + name);
		String template = read(in).trim();
		return template;
	}

	public static void flush(String parentDirectory, String directory,
			String filename, String content) {
		try {
			String dirName = BASE_DIR + SLASH + parentDirectory + SLASH
					+ directory;

			File dir = new File(dirName);
			if (!dir.exists()) {
				dir.mkdir();
			}

			String outputFile = dirName + SLASH + filename;

			// remove double seperators in the file name
			outputFile = outputFile.replace(SLASH + SLASH, SLASH);

			OutputStreamWriter fstream = new OutputStreamWriter(
					new FileOutputStream(outputFile));
			BufferedWriter out = new BufferedWriter(fstream);
			try {
				out.write(content);
				System.out.println(outputFile);
			} finally {
				out.close();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static String read(InputStream in) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line

			while ((strLine = br.readLine()) != null) {
				sb.append("\n").append(strLine);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			// Close the input stream
			try {
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return sb.toString();
	}
}
