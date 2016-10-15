package org.cmdb4j.overthere.shells.sh;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ShellArgumentTokenizerUtilsTest {

	@Test
	public void testTokenize() {
		List<String> res = ShellArgumentTokenizerUtils.tokenize("hello\\ world \"arg1 arg1bis\" hello");
		Assert.assertEquals("hello world", res.get(0));
		Assert.assertEquals("arg1 arg1bis", res.get(1));
		Assert.assertEquals("hello", res.get(2));
		
	}
}
