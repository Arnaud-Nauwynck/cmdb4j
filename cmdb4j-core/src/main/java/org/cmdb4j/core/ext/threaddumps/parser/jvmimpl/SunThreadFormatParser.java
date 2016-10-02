package org.cmdb4j.core.ext.threaddumps.parser.jvmimpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdb4j.core.ext.threaddumps.model.LockThreadLineInfo;
import org.cmdb4j.core.ext.threaddumps.model.MethodThreadLineInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadLineInfo;
import org.cmdb4j.core.ext.threaddumps.parser.ThreadFormatParser;


/**
 * 
 */
public class SunThreadFormatParser implements ThreadFormatParser {

    private static final Pattern threadPattern = Pattern.compile("\"([^\"]+)\" (daemon )?prio=(\\S+) tid=(\\S+) nid=\\S+ ([^\\[]+)");
    private static final Pattern methodPattern = Pattern.compile("\\s+at ([\\p{Alnum}$_.<>]+)\\(([^\\)]*)\\)");
    private static final Pattern lockPattern = Pattern.compile("\\s+- (waiting to lock|waiting on|locked) <(\\p{Alnum}+)> \\(a ([^\\)]+)\\)");

    public SunThreadFormatParser() {
    }

   public ThreadInfo parseThread(String s) {
        ThreadInfo res = new ThreadInfo();
        Matcher matcher = threadPattern.matcher(s);
        if (matcher.lookingAt()) {
            res.setName(matcher.group(1));
            String s1 = matcher.group(2);
           res.setDaemon(s1 != null);
            res.setPriority(matcher.group(3));
            res.setThreadId(matcher.group(4));
            res.setState(matcher.group(5));
        } else {
            System.err.println("parseThread failed on: '" + s + "'");
        }
        return res;
    }

    public ThreadLineInfo parseThreadLine(String s) {
        ThreadLineInfo res;
        Matcher matcher = methodPattern.matcher(s);
        if (matcher.lookingAt()) {
            MethodThreadLineInfo res2 = new MethodThreadLineInfo();
            String s1 = matcher.group(1);
            int i = s1.lastIndexOf('.');
            res2.setClassName(s1.substring(0, i));
            res2.setMethodName(s1.substring(i + 1));
            String s2 = matcher.group(2);
            i = s2.indexOf(':');
            if(i == -1) {
                if(s2.length() == 0)
                    res2.setLocationClass("Unknown");
                else
                    res2.setLocationClass(s2);
            } else {
                res2.setLocationClass(s2.substring(0, i));
                res2.setLocationLineNo(s2.substring(i + 1));
            }
            res = res2;
        } else if((matcher = lockPattern.matcher(s)).lookingAt()) {
        	LockThreadLineInfo res2 = new LockThreadLineInfo();
           res2.setType(LockThreadLineInfo.lookupType(matcher.group(1)));
            res2.setId(matcher.group(2));
            res2.setClassName(matcher.group(3));
            res = res2;
        } else {
            System.err.println("Unknown line: '" + s + "'");
           res = new MethodThreadLineInfo();
        }
        return res;
    }

}
