package ca.concordia.jdeodorant.eclipse.commandline.utility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class IOHelper {

	public static List<String> readFileByLine(String filePath) {
		File file = new File(filePath);
		List<String> lines = null;
		try {
			lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

}
