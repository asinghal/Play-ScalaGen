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
package play.modules.scalagen;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import play.Play;
import play.modules.scalagen.jpa.ModelGenerator;

/**
 * This is the entry point into the generator and is responsible for processing
 * user requests.
 * 
 * @author Aishwarya Singhal
 */
public class Generator {

	public static void main(String[] args) {
		File root = new File(System.getProperty("application.path"));
		Play.init(root, System.getProperty("play.id", ""));

		if (args.length == 0) {
			printHelp();
			return;
		}

		String command = args[0];
		String entityName = args[1];
		Map<String, String> attributes = new HashMap<String, String>();

		// now lets build a map of different attributes specified.
		for (String arg : args) {
			if (arg.indexOf(":") == -1) {
				// this is an unusable entry
				continue;
			}

			// each attribute should be of the form name:type
			String[] entry = arg.split(":");
			// the first element should be the key (attribute name)
			String key = entry[0].trim();
			// the first element should be the value (attribute type)
			String value = entry[1].trim();

			attributes.put(key, value);
		}

		if (command.equals("--model") || command.equals("--model-jpa") || command.equals("--m")) {
			ModelGenerator.generate(entityName, attributes);
		}
		
		if (command.equals("--scaffold") || command.equals("--scaffold-jpa") || command.equals("--s")) {
			ModelGenerator.generate(entityName, attributes);
		}
		
	}

	private static void printHelp() {
		System.out.println();
		System.out.println("Help:");
		System.out.println();
		System.out
				.println("This module can be used to generate Scala code. It can generate JPA based models, CRUD and associated test cases.");
		System.out.println();
		System.out.println("Options:");
		System.out.println("--------");
		System.out
				.println("--scaffold-jpa [entity name] [attribute1:type1]*     Generates the JPA based model and associated CRUD code.");
		System.out
				.println("--model-jpa [entity name] [attribute1:type1]*         Generates the JPA based model. ");
		System.out.println();
		System.out
				.println("Note: --scaffold and --model are shortcuts to generate JPA");
		System.out
				.println("Example: play scalagen:generate --scaffold User name:String login:String role:Role");
		System.out
		.println("For the lazy, the following does the same: play scalagen:g --s User name:String login:String role:Role");
	}
}
