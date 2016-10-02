package org.cmdb4j.core.ext.threaddumps.util;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Map;

import org.cmdb4j.core.ext.threaddumps.analyzer.ThreadDumpUtils;
import org.cmdb4j.core.ext.threaddumps.model.AttributeMapThreadLineInfo;
import org.cmdb4j.core.ext.threaddumps.model.ClassHistogramInfo;
import org.cmdb4j.core.ext.threaddumps.model.ClassHistogramItemInfo;
import org.cmdb4j.core.ext.threaddumps.model.LockStandaloneInfo;
import org.cmdb4j.core.ext.threaddumps.model.LockThreadLineInfo;
import org.cmdb4j.core.ext.threaddumps.model.MethodThreadLineInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpList;
import org.cmdb4j.core.ext.threaddumps.model.ThreadInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadItemInfoVisitor;
import org.cmdb4j.core.ext.threaddumps.model.ThreadLineInfo;



public class DefaultThreadDumpPrinter implements ThreadItemInfoVisitor {

	private XmlWriter out;

	private NumberFormat numberformat = NumberFormat.getInstance();
	{
		numberformat.setMinimumIntegerDigits(3);
	}

	private boolean printSystemThread = false;
	private boolean printLocks = true;
	private boolean printStandaloneLocks = true;
	
	
	// -------------------------------------------------------------------------

	public DefaultThreadDumpPrinter(XmlWriter out) {
		this.out = out;
	}

	// implements ThreadLineInfoVisitor
	// ------------------------------------------------------------------------

	@Override
	public void caseThreadDumpList(ThreadDumpList p) {
		try {
			out.writeText("ThreadDumpList: " + p.getThreadDumps().size() + " thread dumps(s) \n");
						
			for (ThreadDumpInfo td : p.getThreadDumps()) {
				caseThreadDumpInfo(td);
			}
			
			out.writeText("-- end ThreadDumpList\n");
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	@Override
	public void caseThreadDumpInfo(ThreadDumpInfo p) {
		try {
			//        setTimeBefore("-Not in the dump-");
			//        setimeAfter("-Not in the dump-");
			out.writeText("Full thread dump [" + p.getDumpId() + "] : " + p.getThreads().size() + " thread(s)\n");
			
			if (!p.getHeaderComments().isEmpty()) {
				for (String comment : p.getHeaderComments()) {
					out.writeText(comment+ "\n");
				}
			}
//			if (p.getSkipInactiveThread() != 0) {
//				out.writeText("Skip inactive threads count=" + p.getSkipInactiveThread() + "\n"); 
//			}
			
//			AttributesImpl attr = new AttributesImpl();
//			attr.addAttribute(null, "id", null, null,String.valueOf(p.getDumpId()));
//			attr.addAttribute(null, "title", null, null, p.getDumpTitle());
//			attr.addAttribute(null, "before", null, null, p.getTimeBefore());
//			attr.addAttribute(null, "after", null, null, p.getTimeAfter());
//			attr.addAttribut(null, "startLine", null, null, String.valueOf(p.getStartSourceLine()));
//			attr.addAttribute(null, "endLine", null, null, String.valueOf(p.getEndSourceLine()));
//			attr.addAttribute(null, "lines", null, null, String.valueOf((p.getEndSourceLine() - p.getStartSurceLine()) + 1L));
//			attr.addAttribute(null, "threads", null, null, String.valueOf(p.getThreads().size()));
//			out.writeTag("ThreadDump", attr, false);
//
//			if (p.getPreDumpInfo() != null) {
//				out.writeTag("preDump");
//				out.writeText(p.getPreDmpInfo());
//				out.writeCloseTag("preDump");
//			}

			out.writeText("\n");
			out.writeText("Threads\n");
			for (ThreadInfo elt : p.getThreads()) {
				caseThreadInfo(elt);
			}

//			if (printStandaloneLocks && p.getStandaloneLocks() != null && p.gettandaloneLocks().size() != 0) {
//				out.writeTag("standaloneLocks");
//				for (LockStandaloneInfo elt : p.getStandaloneLocks()) {
//					caseLockStandaloneInfo(elt);
//				}
//				out.writeCloseTag("standaloneLocks");
//			}
//
//			if (p.getDeadlockInfo( != null) {
//				out.writeTag("deadlockInfo");
//				out.writeText(p.getDeadlockInfo());
//				out.writeCloseTag("deadlockInfo");
//			}
//			
//			if (p.getPostDumpInfo() != null) {
//				out.writeTag("postDump");
//				out.writeText(p.getPostDumpInfo());
//				out.writeCloseTag("postDump");
//			}

			if (p.getClassHistogramInfo() != null) {
				caseClassHistogramInfo(p.getClassHistogramInfo());
			}
			
			out.writeText("-- End Full thread dump [" + p.getDumpId() + "]\n");
			out.writeText("\n");
			
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}


	@Override
	public void caseThreadInfo(ThreadInfo p) {
		try {
			if (!printSystemThread && ThreadDumpUtils.isSystemThread(p.getName())) {
				return;
			}
			
			out.writeText("\"" + p.getName() + "\" "  
					+ ((p.isDaemon())? "daemon " : "")
					+ "prio=" + p.getPriority() + " "
					+ "tid=" + p.getThreadId() + " "
					//?? + "nid= "
					+ " " + p.getState()
					+ "\n"
			);

			for (MethodThreadLineInfo elt : p.getStackDisplay()) {
				elt.visit(this);
			}
			
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	public void caseMethodThreadLineInfo(MethodThreadLineInfo p) {
		try {
			if (ThreadDumpUtils.isJavaLangObjectWait(p)) {
				out.writeText("\t... wait jva.lang.Object\n");
				return;
			}
					
			
			String location;
			if (p.getLocationClass()!=null) {
				location = "(" + p.getLocationClass() 
					+ ((p.getLocationLineNo() != null) ? ":" + p.getLocationLineNo() : "") 
					+ ")";
			} else {
				location = "";
			}

			if (p.getSkipCount() != 0) {
				out.writeText("\t... skipped " + p.getSkipCount() + "\n");  
			}
			
			out.writeText("\tat " + p.getClassName() + "." + p.getMethodName() + location + "\n");
			
			boolean hasInfo = (p.getInfos() != null && p.getInfos().size() != 0); 
			if (hasInfo) {
				for (ThreadLineInfo elt : p.getInfos()) {
					elt.visit(this);
				}
			}
			
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	@Override
	public void caseAttributeMapThreadLineInfo(AttributeMapThreadLineInfo p) {
		if (p.getAttributes() == null || p.getAttributes().size() == 0) return;
		try {
			for (Map.Entry<String, String> entry : p.getAttributes().entrySet()) {
				out.writeText("\t" + entry.getKey() + "=" + entry.getValue() + "\n");
			}
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	@Override
	public void caseLockThreadLineInfo(LockThreadLineInfo p) {
		if (!printLocks) return;
		
		try {
			String type = (p.getType().equals("locked"))? "a " : "";
			String attr ="<" + p.getId() + "> (" + type + p.getClassName() + ")";
			out.writeText("\t- locked " + attr + "\n");
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	@Override
	public void caseLockStandaloneInfo(LockStandaloneInfo p) {
		if (!printStandaloneLocks) return;

		try {
			String type = (p.getType().equals("locked"))? "a " : "";
			String attr = "<" + p.getId() + "> (" + type + p.getClassName() + ")"
				+ " tid=" + p.getThreadId() 
				+ " threadResolved=" + Boolean.toString(p.isThreadResolved());			
			out.writeText("\t- locked " + attr +"\n");
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	@Override
	public void caseClassHistogramInfo(ClassHistogramInfo p) {
		try {
			out.writeText("ClassHistogramInfo\n");
			for(ClassHistogramItemInfo elt : p.getClassItems()) {
				caseClassHistogramItemInfo(elt);
			}
			out.writeText("Total"
					+ " " + pad(8, Integer.toString(p.getTotalInstanceCount()))
					+ "  " + pad(10, Long.toString(p.getTotalInstanceByteSize()))
					+ "\n");
		} catch(IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	
	public void caseClassHistogramItemInfo(ClassHistogramItemInfo p) {
		try {
			// formatted with spaces as original input  
//			out.writeText(p.getRank() + ": " + p.getInstanceCount() + "  + p.getInstanceByteSize() + " " + p.getClassName() + "\n");
			out.writeText(
					pad(3, Integer.toString(p.getRank()))
					+ ":"
					+ "  " + pad(8, Integer.toString(p.getInstanceCount()))
					+ "  " + pad(10, Long.toString(p.getInstanceByteSize()))
				+ "  " + p.getClassName() 
					+ "\n"
					);
					
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	private static String pad(int size, String value) {
		char[] ch = new char[size];
		Arrays.fill(ch, 0, size, (char) ' ');
	char[] chValue = value.toCharArray();
		int chValueLen = chValue.length; 
		if (chValueLen < size) {
			System.arraycopy(chValue, 0, ch, size-chValueLen, chValueLen);
			return new String(ch);
		} else {
			return value;
		}
	}
	
}

