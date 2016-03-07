package org.cmdb4j.core.hieraparams;

import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.util.PathId;
import org.junit.Assert;
import org.junit.Test;


public class HieraParamsTest {

	protected HieraParams sut = new HieraParams();

	protected PathId abPath = PathId.valueOf("a", "b");
	protected PathId abcPath = abPath.child("c");
	protected PathId dPath = PathId.valueOf("d");
	protected PathId dePath = dPath.child("e");

	@Test
	public void testGetOverride() {
		// Prepare
		// Perform
		String check = sut.getOverride(abPath, "key");
		// Post-check
		Assert.assertNull(check);

		// Prepare
		sut.putOverride(abPath, "key", "value");
		// Perform
		check = sut.getOverride(abPath, "key");
		// Post-check
		Assert.assertEquals("value", check);
	}
	
	@Test
	public void testPutOverride() {
		// Prepare
		// Perform
		String old = sut.putOverride(abPath, "key", "value");
		// Post-check
		Assert.assertNull(old);
		String check = sut.getOverride(abPath, "key");
		Assert.assertEquals("value", check);
	}

	@Test
	public void testPutAllOverrides() {
		// Prepare
		Map<String, String> map = new HashMap<String,String>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		// Perform
		sut.putAllOverrides(abPath, map);
		// Post-check
		Assert.assertEquals("value1", sut.getOverride(abPath, "key1"));
		Assert.assertEquals("value2", sut.getOverride(abPath, "key2"));
	}

	@Test
	public void testRemoveOverride() {
		// Prepare
		sut.putOverride(abPath, "key", "value");
		// Perform
		sut.removeOverride(abPath, "key");
		// Post-check
		Assert.assertNull(sut.getOverride(abPath, "key"));

		// Prepare
		// Perform
		sut.removeOverride(abcPath, "key");
		sut.removeOverride(abPath, "key"); // repeat => do nothing!
		// Post-check
	}

	@Test
	public void testResolveAllParamsFor() {
		// Prepare
		sut.putOverride(abPath, "key1", "value1");
		sut.putOverride(abPath, "key2", "value2");
		sut.putOverride(abcPath, "key1", "value1-abc");
		sut.putOverride(abcPath, "key3", "value3");
		sut.putOverride(dPath, "key4", "value4");
		// Perform
		Map<String, String> res = sut.resolveAllParamsFor(abPath);
		// Post-check
		MapTstUtils.assertEqualsMap(MapTstUtils.map("key1", "value1", "key2", "value2"), res);

		// Perform
		res = sut.resolveAllParamsFor(abcPath);
		// Post-check
		MapTstUtils.assertEqualsMap(MapTstUtils.map(
				"key1", "value1-abc", // overriden in "a/b/c"
				"key2", "value2", // in "a/b"
				"key3", "value3" // in "a/b/c"
				), res);
	
		// Perform
		res = sut.resolveAllParamsFor(abcPath, dPath);
		// Post-check
		MapTstUtils.assertEqualsMap(MapTstUtils.map(
				"key1", "value1-abc", 
				"key2", "value2", 
				"key3", "value3",
				"key4", "value4" // in "d"
				), res);

	}
	
}
