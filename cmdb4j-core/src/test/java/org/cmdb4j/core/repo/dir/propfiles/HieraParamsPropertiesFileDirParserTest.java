package org.cmdb4j.core.repo.dir.propfiles;

import static org.cmdb4j.core.hieraparams.MapTstUtils.assertEqualsOverride;
import static org.cmdb4j.core.hieraparams.MapTstUtils.map;

import java.io.File;

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


}
