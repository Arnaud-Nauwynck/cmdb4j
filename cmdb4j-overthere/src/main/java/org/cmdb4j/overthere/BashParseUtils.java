package org.cmdb4j.overthere;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BashParseUtils {


	public static class ShellVars {
		public Map<String,String> envVars = new LinkedHashMap<>();
		public Map<String,String> localScriptVars = new LinkedHashMap<>();
		
		public String getLocalOrEnvVar(String var) {
			String res = localScriptVars.get(var);
			if (res == null) {
				res = envVars.get(var);
			}
			return res;
		}
	}
	
	/**
	 * parse lines with "export <<VAR>>=<<VALUE>>"... or "<<VAR>>=<<VALUE>>" then export VAR
	 *  heuristic .. do not parse real .sh bash script langage ...
	 * @param script
	 * @return
	 */
	public static void heuristicDetectShellVarsFromScript(ShellVars res, String script) {
		heuristicDetectShellVarsFromScript(res, script, null, null);
	}
	
	public static void heuristicDetectShellVarsFromScript(ShellVars res, String script, String dir, Function<String,String> fileLoader) {
		script = "\n" + script; // HACK to force detect vars on new line
		script = script.replace("\\\n", ""); // line escape continuation
		Map<String,String> envVars = res.envVars;
		Map<String,String> localScriptVars = res.localScriptVars;
		Pattern anyVarPattern = Pattern.compile("(\\n\\s*export\\s+(\\w+)=([^\\n]*))"  //example: 'export VAR="value"' 
				+ "|(\\n\\s*(\\w+)=([^\\n]*)\\s*;\\s*export\\s+([^\\n]*))"  // example:  'VAR="value" ; export VAR"'
				+ "|(\\n\\s*(\\w+)=([^\\n]*))" // example: 'VAR="value"'
				+ "|(\\n\\s*export\\s+([^\\n]*))" // example: 'export VAR'
				+ "|((source|\\.)\\s+([^\\n]*))" // example: "source ./bin/another-setenv.sh"
				);
		Matcher matcher = anyVarPattern.matcher(script);
		for(int pos =  0; matcher.find(pos); ) {
			pos = matcher.start();
			String lineExportVarValue = matcher.group(1);
			String lineVarValueExport = matcher.group(4);
			String lineVarValue = matcher.group(8);
			String lineExportVar = matcher.group(11);
			String lineSourceScript = matcher.group(13);
			
			if (lineExportVarValue != null) {
				// detected line1: 'export VAR="value"' 
				String var = matcher.group(2), value = matcher.group(3);
				String evalValue = unquoteEval(value, res);
				envVars.put(var, evalValue);
				localScriptVars.remove(var);
			} else if (lineVarValueExport != null) {
				// detected line 'VAR="value" ; export VAR"'
				String var = matcher.group(5), value = matcher.group(6), var2 = matcher.group(7);
				String evalValue = unquoteEval(value, res);
				if (var.equals(var2)) {
					envVars.put(var, evalValue);
				} else {
					// suspect line error...
					localScriptVars.put(var, evalValue);
					String exportedLocalValue = localScriptVars.remove(var2);
					if (exportedLocalValue != null) {
						envVars.put(var2, exportedLocalValue);
					} // else suspect error
				}
			} else if (lineVarValue != null) {
				// detected line 'VAR="value"'
				String var = matcher.group(9), value = matcher.group(10);
				String evalValue = unquoteEval(value, res);
				localScriptVars.put(var, evalValue);
			} else if (lineExportVar != null) {
				// detected line 'export VAR'
				String var = matcher.group(12);
				String exportedValue = localScriptVars.remove(var);
				if (exportedValue != null) {
					envVars.put(var, exportedValue);
				} // else suspect error
			} else if (lineSourceScript != null) {
				// detected 'source setenv.sh'
				String sourceScriptFile = matcher.group(15);
				sourceScriptFile = unquoteEval(sourceScriptFile, res);
				if (fileLoader != null) {
					String sourceScriptContent;
					try {
						// load sourced script file content (as relative in dir)
						String sourceScriptPath = (dir != null && !dir.isEmpty())? dir + "/" + sourceScriptFile : sourceScriptFile;
						sourceScriptContent = fileLoader.apply(sourceScriptPath);
					} catch(Exception ex) {
						// error: Failed to load sourced file
						sourceScriptContent = null;
					}
						
					if (sourceScriptContent != null) {
						String relativeScriptDir = dir;
						int lastSlash = sourceScriptFile.lastIndexOf('/');
						if (lastSlash != -1) {
							relativeScriptDir = sourceScriptContent.substring(0, lastSlash);
							if (relativeScriptDir.equals(".")) {
								relativeScriptDir = "";
							}
						}
						String childDir = (dir != null && !dir.isEmpty())? dir + "/" + relativeScriptDir : relativeScriptDir;
						// recurse
						heuristicDetectShellVarsFromScript(res, sourceScriptContent, childDir, fileLoader);
					} // else error: no sourced file
					
				}
			}
			
			pos = matcher.end(); // + 1;
			if (pos == script.length()) {
				break;
			}
		}
	}
	
	protected static final Pattern quotedPattern = Pattern.compile("\"(.*)\"\\s*");
	protected static final Pattern singleQuotedPattern = Pattern.compile("'(.*)'\\s*");
	
	public static String unquote(String text) {
		if (text == null) {
			return null;
		}
		String res = text;
		Matcher m = quotedPattern.matcher(res);
		if (m.matches()) {
			res = m.group(1);
		} else {
			m = singleQuotedPattern.matcher(res);
			if (m.matches()) {
				res = m.group(1);
			}
		}
		return res;
	}
	
	public static String unquoteEval(String text, ShellVars repl) {
		if (text == null) {
			return null;
		}
		String res = text;
		Matcher m = quotedPattern.matcher(res);
		if (m.matches()) {
			res = m.group(1);
			// eval inside double quote text.. exemple: with a=1 bc=2  "$a ${a} $bc ${bc}" ==> "1 1 2 2"
			res = evalVars(res, repl);
		} else {
			m = singleQuotedPattern.matcher(res);
			if (m.matches()) {
				res = m.group(1);
			}
		}
			
		return res;
	}
	
	public static String evalVars(String text, ShellVars repl) {
		Map<String,String> vars = new HashMap<>();
		vars.putAll(repl.envVars);
		vars.putAll(repl.localScriptVars);
		return evalVars(text, vars);
	}

	public static String evalVars(String text, Map<String,String> vars) {
		StringBuilder res = new StringBuilder();
		int pos = 0;
		for(;;) {
			int prevPos = pos;
			pos = text.indexOf("$", pos);
			if (pos == -1) {
				if (prevPos < text.length()) {
					res.append(text.substring(prevPos, text.length()));
				}
				break;
			}
			if (pos != 0 && prevPos < pos) {
				res.append(text.substring(prevPos, pos));
			}
			if (pos > 0 && text.charAt(pos-1) == '\\') {
				// escaped $: '\$'
				pos = pos+1;
				continue;
			}
			if (pos == text.length()-1) {
				// ends with '$' ?
				break;
			}
			if (text.charAt(pos+1) == '{') {
				int endPos = text.indexOf('}', pos+1);
				if (endPos == -1) {
					res.append(text.substring(pos+1, text.length()));
					break; // suspect missing closing '}'   example: "${abc def"
				}
				String var = text.substring(pos+2, endPos);
				String value = vars.get(var);
				if (value != null) {
					// found var => replace ${var} by value
					res.append(value);
					pos = endPos + 1;
					continue;
				} else {
					// var not found, leave text ${var}
					res.append(text.substring(pos, endPos+1));
					pos = endPos + 1;
					continue;
				}
			} else {
				// lookup var as long as possible ...  (example: A=1; AB=2; echo "$AB" => 2 ... not 1B )
				int maxI = text.length();
				for(int i = pos+1; i < maxI; i++) {
					if (Character.isWhitespace(text.charAt(i))) {
						maxI = i;
						break; // or space is protected by '\ ' !..
					}
				}
				String foundValue = null;
				for(int i = maxI; i >= pos+1; i--) {
					String tryVar = text.substring(pos+1, i);
					String value = vars.get(tryVar);
					if (value != null) {
						res.append(value);
						pos = i;
						foundValue = value;
						break;
					}
				}
				if (foundValue == null) {
					// var not found, leave text "$var " 
					res.append(text.substring(pos, maxI));
					pos = maxI;
					continue;
				}
			}
			
		}
		return res.toString();
	}
	
}
