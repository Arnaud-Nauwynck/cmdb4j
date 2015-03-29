package org.cmdb4j.core.hieraparams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class HieraPathTest {

	protected HieraPath rootPath = HieraPath.emptyPath();
	protected HieraPath aPath = rootPath.child("a");
	protected HieraPath abPath = rootPath.child("a", "b");
	protected HieraPath abcPath = rootPath.child("a", "b", "c");

	protected HieraPath bPath = rootPath.child("b");

	@Test
	public void testValueOf() {
		Assert.assertEquals(HieraPath.valueOf("a/b/c/d/e"), HieraPath.valueOf("a/b/c", "d/e"));
	}
	
	@Test
	public void testParent() {
		Assert.assertEquals(rootPath, rootPath.parent());
		Assert.assertEquals(rootPath, aPath.parent());
		Assert.assertEquals(aPath, abPath.parent());
	}
	
	@Test
	public void testSubPath() {
		Assert.assertEquals(bPath, abPath.subPath(1, 2));
		Assert.assertEquals(aPath, abPath.subPath(0, 1));
		Assert.assertSame(abPath, abPath.subPath(0, 2));
	}

	@Test
	public void testChild() {
		Assert.assertEquals(abPath, aPath.child("b"));
		Assert.assertEquals(abPath, aPath.child(bPath));
	}

	@Test
	public void testSize() {
		Assert.assertEquals(0, rootPath.size());
		Assert.assertEquals(1, aPath.size());
		Assert.assertEquals(2, abPath.size());
	}
	
	@Test
	public void testGet() {
		Assert.assertEquals("a", abPath.get(0));
		Assert.assertEquals("b", abPath.get(1));
	}
	
	@Test
	public void testToList() {
		Assert.assertTrue(rootPath.toList().isEmpty());
		List<String> res = abPath.toList();
		Assert.assertEquals("a", res.get(0));
		Assert.assertEquals("b", res.get(1));
	}
	
	@Test
	public void testHashcode() {
		Assert.assertEquals(abPath.hashCode(), HieraPath.valueOf("a", "b").hashCode());
		Assert.assertNotEquals(aPath.hashCode(), abPath.hashCode());
	}

	@Test
	public void testEquals() {
		Assert.assertFalse(abPath.equals(new Object()));
		Assert.assertFalse(abPath.equals("a/b"));
		Assert.assertFalse(abPath.equals(null));
		Assert.assertTrue(abPath.equals(abPath));
		Assert.assertEquals(abPath, HieraPath.valueOf("a", "b"));
		Assert.assertNotEquals(abPath, aPath);
	}

	@Test
	public void testCompareTo() {
		Assert.assertEquals(-1, aPath.compareTo(abPath));
		Assert.assertEquals(0, abPath.compareTo(abPath));
		Assert.assertEquals(+1, abPath.compareTo(aPath));

		Assert.assertEquals(-1, aPath.compareTo(bPath));		
	}

	@Test
	public void testToString() {
		Assert.assertEquals("", rootPath.toString());
		Assert.assertEquals("a", aPath.toString());
		Assert.assertEquals("a/b", abPath.toString());
	}


}
