package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import main.Util;

import org.junit.Test;

public class UtilTest {

	@Test
	public void testReadWrite() {
		Path path = Paths.get("testFile");
		try {
			Files.deleteIfExists(path);//clean
			Util.writeToFile("testFile", "test Content");
			assertTrue(Files.exists(path));
			assertEquals("test Content" + System.lineSeparator(), Util.readFromFile("testFile"));
			path.toFile().deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
