package org.cmdb4j.core.hieraparams;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

public class MapTstUtils {

	
	
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
