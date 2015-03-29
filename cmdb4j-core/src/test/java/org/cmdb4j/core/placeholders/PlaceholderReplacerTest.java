package org.cmdb4j.core.placeholders;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.cmdb4j.core.placeholders.PlaceholderReplacer;
import org.cmdb4j.core.util.PatternFileFilter;
import org.cmdb4j.core.util.SuffixNameFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PlaceholderReplacerTest {
	
	
	private PlaceholderReplacer sut;
	
	@Before
	public void setup() {
		Map<String,String> filterContext = new HashMap<String,String>();
		filterContext.put("World", "World");
		
		List<Pattern> noCopyPatterns = Arrays.asList(new Pattern[] { 
				Pattern.compile("\\..*"),
				Pattern.compile("ignore-.*")
		});
		FileFilter noCopyFiles = new PatternFileFilter(noCopyPatterns);
		
		List<String> noReplaceSuffixes = Arrays.asList(new String[] { ".dat" });
		FileFilter noReplaceFiles = new SuffixNameFileFilter(noReplaceSuffixes); 
		
		sut = new PlaceholderReplacer(filterContext, noCopyFiles, noReplaceFiles);
	}
	
	@Test
	public void testCopy_dir1() {
		// Prepare
		File fromDir = new File("src/test/dirs/dir1");
		File toDir = new File("target/test/dirs/dir1");
		if (toDir.exists()) {
			toDir.delete();
		}
		toDir.mkdirs();
		// Perform
		sut.filterCopyDir(fromDir, toDir);
		// Post-check
		Assert.assertFalse(new File(toDir, "ignore-copy.txt").exists());
		assertFileEqualsContent(new File(fromDir, "no-replace.dat"), new File(toDir, "no-replace.dat"));
		assertFileEqualsContent("Hello World", new File(toDir, "file1.txt"));
	}

	public static void assertFileEqualsContent(String expected, File actualFile) {
		byte[] actualBytes;
		try {
			actualBytes = Files.readAllBytes(actualFile.toPath());
		} catch (IOException e) {
			throw new RuntimeException("", e);
		}
		Assert.assertEquals(expected, new String(actualBytes));
	}

	public static void assertFileEqualsContent(File expectedFile, File actualFile) {
		try {
			byte[] expectedBytes = Files.readAllBytes(expectedFile.toPath());
			byte[] actualBytes = Files.readAllBytes(actualFile.toPath());
			assertBytesEquals(expectedBytes, actualBytes);
		} catch (IOException e) {
			throw new RuntimeException("", e);
		}
	}

	private static void assertBytesEquals(byte[] expected, byte[] actual) {
		Assert.assertEquals(expected.length, actual.length);
		for(int i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], actual[i]);
		}
	}

}
