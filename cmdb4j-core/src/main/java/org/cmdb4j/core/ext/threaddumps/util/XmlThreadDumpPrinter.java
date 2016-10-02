package org.cmdb4j.core.ext.threaddumps.util;


import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;

import org.xml.sax.helpers.AttributesImpl;

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



public class XmlThreadDumpPrinter implements ThreadItemInfoVisitor {

	private XmlWriter out;

	private NumberFormat numberformat = NumberFormat.getInstance();
	{
		numberformat.setMinimumIntegerDigits(3);
	}

	private boolean printSystemThread = false;
	private boolean printLocks = true;
	private boolean printStandaloneLocks = true;
	
	
	// -------------------------------------------------------------------------

	public XmlThreadDumpPrinter(XmlWriter out) {
		this.out = out;
	}

	// implements ThreadLineInfoVisitor
	// -------------------------------------------------------------------------

	public void caseThreadDumpList(ThreadDumpList p) {
		try {			
			out.writeTag("ThreadDupList");
			
			for (ThreadDumpInfo td : p.getThreadDumps()) {
				caseThreadDumpInfo(td);
			}
			
			out.writeCloseTag("ThreadDumpList");
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	public void caseThreadDumpInfo(ThreadDumpInfo p) {
		try {
			//        setTimeBefore("-Not in the dump-");
			//        setTimeAfter("-Not in the dump-");
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute(null, "id", null, null, String.valueOf(p.getDumpId()));
			attr.addAttribute(null, "title", null, null, p.getDumpTitle());
			attr.addAttribute(null, "before", null, null, p.getTimeBefore());
			attr.addAttribute(null, "after", null, null, p.getTimeAfter());
			attr.addAttribute(null, "startLine", null, null, String.valueOf(p.getStartSourceLine()));
			attr.addAttribute(null, "endLine", null, null, String.valueOf(p.getEndSourceLine()));
			attr.addAttribute(null, "lines", null, null, String.valueOf((p.getEndSourceLine() - p.getStartSourceLine()) + 1L));
			attr.addAttribute(null, "threads", null, null, String.valueOf(p.getThreads().size()));
			out.writeTag("ThreadDump", attr, false);

			if (!p.getHeaderComments().isEmpty()) {
				out.writeTag("headerComments");
				for (String comment : p.getHeaderComments()) {
					out.writeText(comment + "\n");
			}
				out.writeCloseTag("headerComments");
			}
			
			if (p.getPreDumpInfo() != null) {
				out.writeTag("preDump");
				out.writeText(p.getPreDumpInfo());
				out.writeCloseTag("preDump");
			}

			for (ThreadInfo elt : p.getThreads()) {
				caseThreadInfo(elt);
			}

			if (printStandaloneLocks && p.getStandaloneLocks() != null && p.getStandaloneLocks().size() != 0) {
				out.writeTag("standaloneLocks");
				for (LockStandaloneInfo elt : p.getStandaloneLocks()) {
					caseLockStandaloneInfo(elt);
				}
			out.writeCloseTag("standaloneLocks");
			}

			if (p.getDeadlockInfo() != null) {
				out.writeTag("deadlockInfo");
				out.writeText(p.getDeadlockInfo());
				out.writeCloseTag("deadlockInfo");
			}
			
			if (p.getPostDumpInfo() != null) {
				out.writeTag("postDump");
				out.writeText(p.getPostDumpInfo());
				out.writeCloseTag("postDump");
			}

			out.writeCloseTag("ThreadDump");
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}


	public void caseThreadInfo(ThreadInfo p) {
		try {
			if (!printSystemThread && ThreadDumpUtils.isSystemThread(p.getName())) {
				return;
			}
			
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute(null, "name", null, null, p.getName());
			attr.addAttribute(null, "pri", null, null,p.getPriority()); 
			attr.addAttribute(null, "id", null, null, p.getThreadId()); 
			attr.addAttribute(null, "state", null, null, p.getState());
			if (p.isDaemon()) {
				attr.addAttribute(null, "daemon", null, null, "true");
			}
			out.writeTag("thread", attr, false);

			out.writeTag("stack", null, false);
			for (MethodThreadLineInfo elt : p.getStackDisplay()) {
				elt.visit(this);
			}
			out.writeCloseTag("stack");
			
			out.writeCloseTag("thread");
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	public void caseMethodThreadLineInfo(MethodThreadLineInfo p) {
		try {
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute(null, "className", null, null, p.getClassName());
			attr.addAttribute(null, "methodName", null, null, p.getMethodName());
			attr.addAttribute(null, "location", null, null, p.getLocationClass());
			if (p.getLocationLineNo() != null) {
				attr.addAttribute(null, "locationLine", null, null, p.getLocationLineNo());
			}
			
			boolean hasInfo = (p.getInfos() != null && p.getInfos().size() != 0); 
			if (hasInfo) {
				out.writeTag("method", attr, false);

				out.writeTag("infos", null, false);
				for (ThreadLineInfo elt : p.getInfos()) {
					elt.visit(this);
				}
				out.writeCloseTag("infos");
				
			out.writeCloseTag("method");
			} else {
				out.writeTag("method", attr, true);
			}
			
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	public void caseAttributeMapThreadLineInfo(AttributeMapThreadLineInfo p) {
		if (p.getAttributes() == null || p.getAttributes().size() == 0) return;
		try {
			for (Map.Entry<String, String> entry : p.getAttributes().entrySet()) {
				AttributesImpl attr = new AttributesImpl();
				attr.addAttribute(null, "name", null, null, String.valueOf(entry.getKey()));
				attr.addAttribute(null, "value", null, null, String.valueOf(entry.getValue()));
				out.writeTag("attrib", attr, true);
			}
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	public void caseLockThreadLineInfo(LockThreadLineInfo p) {
		if (!printLocks) return;
		
		try {
			AttributesImpl attr = new AttributesImpl();
			doFillAttrLockThreadLineInfo(attr, p);
			out.writeTag("lock", attr, true);
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	protected void doFillAttrLockThreadLineInfo(AttributesImpl attr, LockThreadLineInfo p) {
		attr.addAttribute(null, "type", null, null, p.getType());
		attr.addAttribute(null, "id", null, null, p.getId());
		attr.addAttribute(null, "className", null, null, p.getClassName());
	}
	
	public void caseLockStandaloneInfo(LockStandaloneInfo p) {
		if (!printStandaloneLocks) return;

		try {
			AttributesImpl attr = new AttributesImpl();
			doFillAttrLockThreadLineInfo(attr, p);
			attr.addAttribute(null, "threadId", null, null, p.getThreadId());
			attr.addAttribute(null, "threadResolved", null, null, Boolean.toString(p.isThreadResolved()));
			
			out.writeTag("lockStandalone", attr, true);
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}


	public void caseClassHistogramInfo(ClassHistogramInfo p) {
		try {
			AttributesImpl attr = new AttributesImpl();
			out.writeTag("classHistogramInfo", attr, false);
			
			for(ClassHistogramItemInfo elt : p.getClassItems()) {
				caseClassHistogramItemInfo(elt);
		}
			
			out.writeCloseTag("classHistogramInfo");
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	public void caseClassHistogramItemInfo(ClassHistogramItemInfo p) {
		try {
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute(null, "rank", null, null, Integer.toString(p.getRank()));
			attr.addAttribute(null, "count", null, null, Integer.toString(p.getInstanceCount()));
			attr.addAttribute(null, "byteSize", null, null, Long.toString(p.getInstanceByteSize()));
			attr.addAttribute(null, "className", null, null, p.getClassName());
			
			out.writeTag("classItem", attr, true);  
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}
	
	protected static String protectAttr(String p) {
		String res = p;
		if (res != null) {
			res = res.replace('\"', '\'');
		}
		return res;
	}
	
}