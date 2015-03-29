package org.cmdb4j.core.hieraparams;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


public class HieraParamsTest {

	protected HieraParams sut = new HieraParams();
	protected HieraPath abPath = HieraPath.valueOf("a", "b");

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
	}

}
