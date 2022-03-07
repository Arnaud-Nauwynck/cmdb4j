package org.cmdb4j.core.env.input;

import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import lombok.Getter;

/**
 * immutable config for 1 input dir
 *
 */
@Getter
public final class InputDirConfig {
    
    private final String name;
    
    private final String checkoutDir;// optionnal, default to "<<baseDir>>/<<name>>"

    private final String scmUrl;
    private final String scmUserName;
    private final String scmUserCredential;
    
    private final String dept;
    private final String team;
    
    private final ImmutableList<String> includes;
    private final ImmutableList<String> excludes;
    
    // ------------------------------------------------------------------------

    public InputDirConfig(
            @JsonProperty("name") String name,
            @JsonProperty("checkoutDir") String checkoutDir,
            @JsonProperty("scmUrl") String scmUrl,
            @JsonProperty("scmUserName") String scmUserName,
            @JsonProperty("scmUserCredential") String scmUserCredential,
            @JsonProperty("dept") String dept,
            @JsonProperty("team") String team,
            @JsonProperty("includes") List<String> includes,
            @JsonProperty("excludes") List<String> excludes) {
        this.name = name;
        this.checkoutDir = checkoutDir;
        this.scmUrl = scmUrl;
        this.scmUserName = scmUserName;
        this.scmUserCredential = scmUserCredential;
        this.dept = dept;
        this.team = team;
        this.includes = (includes != null)? ImmutableList.copyOf(includes) : ImmutableList.of(); 
        this.excludes = (excludes != null)? ImmutableList.copyOf(excludes) : ImmutableList.of(); 
    }

    // ------------------------------------------------------------------------
    
    public boolean acceptTextIncludesExcludes(String text) {
        boolean res = true;
        if (includes != null && !includes.isEmpty()) {
            res = (null != findFirstMatch(includes, text));
        }
        if (res) {
            if (excludes != null && ! excludes.isEmpty()) {
                res = (null == findFirstMatch(excludes, text));
            }
        }
        return res;
    }

    private static String findFirstMatch(List<String> patternTexts, String text) {
        for(String patternText : patternTexts) {
            Pattern p = Pattern.compile(patternText);
            if (p.matcher(text).matches()) {
                return patternText;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return "InputDirConfig [" + name + ", scmUrl=" + scmUrl 
        		+ ", dept:" + dept + ", team:" + team
                + ((! includes.isEmpty())? ", includes=" + includes : "") 
                + ((! excludes.isEmpty())? ", excludes=" + excludes : "") 
                + "]";
    }
    
    
}