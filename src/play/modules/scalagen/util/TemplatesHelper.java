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
package play.modules.scalagen.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * <p>General file processing (read/ write) utilities.</p>
 * 
 * @author Aishwarya Singhal
 */
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
