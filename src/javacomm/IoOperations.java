package javacomm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class IoOperations {
	public static void writeToFile(String output_file, List<String> content) throws IOException {
		Path output_path = Paths.get(output_file);
		Files.createDirectories(output_path.getParent());
		Files.write(output_path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
	}
}
