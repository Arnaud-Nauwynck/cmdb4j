package org.cmdb4j.core.repo.dir.propfiles;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.hieraparams.HieraParams;
import org.cmdb4j.core.hieraparams.HieraPath;
import org.junit.Assert;
import org.junit.Test;


public class HieraParamsPropertiesFileDirParserTest {
	
	private HieraParamsPropertiesFileDirParser sut = HieraParamsPropertiesFileDirParser.defaultParamsPropParser();
	
	@Test
	public void testLoadHieraFromDir() {
		File rootDir = new File("src/test/dirs/hiera-prop-dir1");
		// Prepare
		// Perform
		HieraParams res = sut.loadHieraFromDir(rootDir);
		// Post-check
		Assert.assertNotNull(res);
		HieraPath rootPath = HieraPath.emptyPath();
		assertEqualsOverride(map("global.key", "global.key from /"), res, rootPath);
		
		HieraPath defaultPath = rootPath.child("Default");
		HieraPath defaultTeam1Path = defaultPath.child("team1");
		assertEqualsOverride(map("app.key1", "app.key1 value from Default/team1"), res, defaultTeam1Path); 
		HieraPath defaultTeam1App1Path = defaultTeam1Path.child("app1");
		assertEqualsOverride(map("app.key1", "app.key1 value from Default/team1/app1"), res, defaultTeam1App1Path); 
		
		HieraPath dev1Path = rootPath.child("DEV1");
		HieraPath dev1Team1Path = dev1Path.child("team1");
		assertEqualsOverride(map("app.key1", "app.key1 value from DEV1/team1"), res, dev1Team1Path); 
		HieraPath dev1Team1App1Path = dev1Team1Path.child("app1");
		assertEqualsOverride(map("app.key1", "app.key1 value from DEV1/team1/app1"), res, dev1Team1App1Path); 
	}

	
	
	public static Map<String,String> map(String... keyValues) {
		Map<String,String> res = new HashMap<String,String>();
		if (keyValues.length % 2 != 0) throw new IllegalArgumentException();
		for (int i = 0; i+1 < keyValues.length; i+=2) {
			res.put(keyValues[i], keyValues[i+1]);
		}
		return res;
	}
	
	public static void assertEqualsOverride(Map<String,String> expected, HieraParams actualParams, HieraPath path){
		Map<String, String> actualOverrides = actualParams.getPathOverridesOrNull(path);
		if (expected == null || expected.isEmpty()) {
			Assert.assertTrue(actualOverrides == null || actualOverrides.isEmpty());
		} else {
			assertEqualsMap(expected, actualOverrides);
		}
	}
	
	public static void assertEqualsMap(Map<String,String> expected, Map<String,String> actual) {
		if (expected == null || expected.isEmpty()) {
			Assert.assertTrue(actual == null || actual.isEmpty());
		} else {
			Assert.assertEquals(expected.size(), actual.size());
			for(Map.Entry<String,String> e : expected.entrySet()) {
				String key = e.getKey();
				String expectedValue = e.getValue();
				String actualValue = actual.get(key);
				Assert.assertEquals(expectedValue, actualValue);
			}
		}
	}
	
}
