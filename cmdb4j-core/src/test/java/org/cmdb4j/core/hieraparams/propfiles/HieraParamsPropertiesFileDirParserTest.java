package org.cmdb4j.core.hieraparams.propfiles;

import static org.cmdb4j.core.hieraparams.MapTstUtils.assertEqualsOverride;
import static org.cmdb4j.core.hieraparams.MapTstUtils.map;

import java.io.File;

import org.cmdb4j.core.hieraparams.HieraParams;
import org.cmdb4j.core.util.PathId;
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
		PathId rootPath = PathId.emptyPath();
		assertEqualsOverride(map("global.key", "global.key from /"), res, rootPath);
		
		PathId defaultPath = rootPath.child("Default");
		PathId defaultTeam1Path = defaultPath.child("team1");
		assertEqualsOverride(map("app.key1", "app.key1 value from Default/team1"), res, defaultTeam1Path); 
		PathId defaultTeam1App1Path = defaultTeam1Path.child("app1");
		assertEqualsOverride(map("app.key1", "app.key1 value from Default/team1/app1"), res, defaultTeam1App1Path); 
		
		PathId dev1Path = rootPath.child("DEV1");
		PathId dev1Team1Path = dev1Path.child("team1");
		assertEqualsOverride(map("app.key1", "app.key1 value from DEV1/team1"), res, dev1Team1Path); 
		PathId dev1Team1App1Path = dev1Team1Path.child("app1");
		assertEqualsOverride(map("app.key1", "app.key1 value from DEV1/team1/app1"), res, dev1Team1App1Path); 
	}


}
