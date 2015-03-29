package org.cmdb4j.core.placeholders;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.cmdb4j.core.util.IOUtils;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template;

public class PlaceholderReplacer {

	private Map<String,String> filterContext;
	
	private FileFilter noCopyFiles;
	
	private FileFilter noReplaceFiles;

	private Compiler mustacheCompiler;
	
	// ------------------------------------------------------------------------
	
	public PlaceholderReplacer(Map<String, String> filterContext, FileFilter noCopyFiles, FileFilter noReplaceFiles) {
		this.filterContext = filterContext;
		this.noCopyFiles = noCopyFiles;
		this.noReplaceFiles = noReplaceFiles;
		this.mustacheCompiler = Mustache.compiler().strictSections(false);
	}
	
	// ------------------------------------------------------------------------
	
	public void filterCopyDir(File from, File to) {
		IOUtils.checkDirExists(from);
		if (! to.exists()) {
			to.mkdirs();
		} else {
			IOUtils.checkDirExists(to);
		}
	
		recursiveCopy(from, to);
	}

	
	private void recursiveCopy(File fromDir, File toDir) {
		File[] fromChildFiles = fromDir.listFiles();
		if (fromChildFiles != null) {
			for(File fromChildFile : fromChildFiles) {
				String fromChildName = fromChildFile.getName();
				if (isFileIgnored(fromChildFile)) {
					continue;
				}
				String toFileName = replaceText(fromChildName);
				File toChildFile = new File(toDir, toFileName);
				
				if (fromChildFile.isDirectory()) {
					if (toChildFile.exists()) {
						if (! toChildFile.isDirectory()) {
							// delete file, recreate dir
							toChildFile.delete(); 
							toChildFile.mkdir();
						}
					} else { // ! toChildFile.exists()
						toChildFile.mkdir();
					}
					// recurse
					recursiveCopy(fromChildFile, toChildFile);
				} else {
					filterCopyFile(fromChildFile, toChildFile);
				}
			}
		}
	}

	public void filterCopyFile(File fromFile, File toFile) {
		if (isFileFiltered(fromFile)) {
			Template template = compileFileToTemplate(mustacheCompiler, fromFile);
			writeTemplateToFile(template, toFile, filterContext);
		} else {
			// simply copy, not filter!
			IOUtils.copyFile(fromFile, toFile);
		}
	}

	public static void writeTemplateToFile(Template template, File toFile, Object filterContext) {
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(toFile)));
			template.execute(filterContext, writer);
		} catch(IOException ex) {
			throw new RuntimeException("Failed to read file template", ex);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public static Template compileFileToTemplate(Compiler mustacheCompiler, File file) {
		Template res;
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			res = mustacheCompiler.compile(reader);
		} catch(IOException ex) {
			throw new RuntimeException("Failed to read file template", ex);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return res;
	}

	private String replaceText(String text) {
		return text; // TODO
	}

	protected boolean isFileIgnored(File file) {
		return noCopyFiles != null && noCopyFiles.accept(file);
	}

	protected boolean isFileFiltered(File file) {
		return noReplaceFiles == null || ! noReplaceFiles.accept(file);
	}

}
