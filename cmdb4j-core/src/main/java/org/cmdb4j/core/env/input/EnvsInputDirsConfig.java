package org.cmdb4j.core.env.input;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import lombok.val;


public final class EnvsInputDirsConfig {

    private final ImmutableList<InputDirConfig> inputDirs;
    
    // ------------------------------------------------------------------------

    public EnvsInputDirsConfig(
            @JsonProperty("inputDirs") List<InputDirConfig> inputDirs) {
        this.inputDirs = ImmutableList.copyOf(inputDirs);
    }

    public static EnvsInputDirsConfig createDefaultForDir(File baseEnvsDir) {
    	val inputDir0 = new InputDirConfig("<input>", baseEnvsDir.getAbsolutePath(), 
    			null, null, null, // scmUrl, scmUsername, scmUserCredential,
                null, null, // dept, team,
                null, null); // includes, excludes
		val inputDirs = ImmutableList.<InputDirConfig>of(inputDir0);
    	return new EnvsInputDirsConfig(inputDirs);
    }
    
    // ------------------------------------------------------------------------

    public ImmutableList<InputDirConfig> getInputDirs() {
        return inputDirs;
    }


    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "EnvsInputConfig[" + inputDirs + "]";
    }

}
