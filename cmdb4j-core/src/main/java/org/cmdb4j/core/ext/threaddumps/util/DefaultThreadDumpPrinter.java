package org.cmdb4j.core.ext.threaddumps.util;

import java.io.PrintStream;
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

	private PrintStream out;

	private NumberFormat numberformat = NumberFormat.getInstance();
	{
		numberformat.setMinimumIntegerDigits(3);
	}

	private boolean printSystemThread = false;
	private boolean printLocks = true;
	private boolean printStandaloneLocks = true;

	// -------------------------------------------------------------------------

	public DefaultThreadDumpPrinter(PrintStream out) {
		this.out = out;
	}

	// implements ThreadLineInfoVisitor
	// ------------------------------------------------------------------------

	@Override
	public void caseThreadDumpList(ThreadDumpList p) {
		println("ThreadDumpList: " + p.getThreadDumps().size() + " thread dumps(s)");

		for (ThreadDumpInfo td : p.getThreadDumps()) {
			caseThreadDumpInfo(td);
		}

		println("-- end ThreadDumpList");
	}

	@Override
	public void caseThreadDumpInfo(ThreadDumpInfo p) {
		// setTimeBefore("-Not in the dump-");
		// setimeAfter("-Not in the dump-");
		println("Full thread dump [" + p.getDumpId() + "] : " + p.getThreads().size() + " thread(s)");

		if (!p.getHeaderComments().isEmpty()) {
			for (String comment : p.getHeaderComments()) {
				println(comment);
			}
		}
		// if (p.getSkipInactiveThread() != 0) {
		// println("Skip inactive threads count=" + p.getSkipInactiveThread());
		// }

		// AttributesImpl attr = new AttributesImpl();
		// attr.addAttribute(null, "id", null,
		// null,String.valueOf(p.getDumpId()));
		// attr.addAttribute(null, "title", null, null, p.getDumpTitle());
		// attr.addAttribute(null, "before", null, null, p.getTimeBefore());
		// attr.addAttribute(null, "after", null, null, p.getTimeAfter());
		// attr.addAttribut(null, "startLine", null, null,
		// String.valueOf(p.getStartSourceLine()));
		// attr.addAttribute(null, "endLine", null, null,
		// String.valueOf(p.getEndSourceLine()));
		// attr.addAttribute(null, "lines", null, null,
		// String.valueOf((p.getEndSourceLine() - p.getStartSurceLine()) +
		// 1L));
		// attr.addAttribute(null, "threads", null, null,
		// String.valueOf(p.getThreads().size()));
		// out.writeTag("ThreadDump", attr, false);
		//
		// if (p.getPreDumpInfo() != null) {
		// out.writeTag("preDump");
		// print(p.getPreDmpInfo());
		// out.writeCloseTag("preDump");
		// }

		println("");
		println("Threads");
		for (ThreadInfo elt : p.getThreads()) {
			caseThreadInfo(elt);
		}

		// if (printStandaloneLocks && p.getStandaloneLocks() != null &&
		// p.gettandaloneLocks().size() != 0) {
		// out.writeTag("standaloneLocks");
		// for (LockStandaloneInfo elt : p.getStandaloneLocks()) {
		// caseLockStandaloneInfo(elt);
		// }
		// out.writeCloseTag("standaloneLocks");
		// }
		//
		// if (p.getDeadlockInfo( != null) {
		// out.writeTag("deadlockInfo");
		// print(p.getDeadlockInfo());
		// out.writeCloseTag("deadlockInfo");
		// }
		//
		// if (p.getPostDumpInfo() != null) {
		// out.writeTag("postDump");
		// print(p.getPostDumpInfo());
		// out.writeCloseTag("postDump");
		// }

		if (p.getClassHistogramInfo() != null) {
			caseClassHistogramInfo(p.getClassHistogramInfo());
		}

		println("-- End Full thread dump [" + p.getDumpId() + "]");
		println("");
	}

	@Override
	public void caseThreadInfo(ThreadInfo p) {
		if (!printSystemThread && ThreadDumpUtils.isSystemThread(p.getName())) {
			return;
		}

		println("\"" + p.getName() + "\" " + ((p.isDaemon()) ? "daemon " : "") + "prio=" + p.getPriority() + " "
				+ "tid=" + p.getThreadId() + " "
				// ?? + "nid= "
				+ " " + p.getState());

		for (MethodThreadLineInfo elt : p.getStackDisplay()) {
			elt.visit(this);
		}
	}

	public void caseMethodThreadLineInfo(MethodThreadLineInfo p) {
		if (ThreadDumpUtils.isJavaLangObjectWait(p)) {
			println("\t... wait java.lang.Object");
			return;
		}

		String location;
		if (p.getLocationClass() != null) {
			location = "(" + p.getLocationClass() + ((p.getLocationLineNo() != null) ? ":" + p.getLocationLineNo() : "")
					+ ")";
		} else {
			location = "";
		}

		if (p.getSkipCount() != 0) {
			println("\t... skipped " + p.getSkipCount());
		}

		println("\tat " + p.getClassName() + "." + p.getMethodName() + location);

		boolean hasInfo = (p.getInfos() != null && p.getInfos().size() != 0);
		if (hasInfo) {
			for (ThreadLineInfo elt : p.getInfos()) {
				elt.visit(this);
			}
		}
	}

	@Override
	public void caseAttributeMapThreadLineInfo(AttributeMapThreadLineInfo p) {
		if (p.getAttributes() == null || p.getAttributes().size() == 0)
			return;
		for (Map.Entry<String, String> entry : p.getAttributes().entrySet()) {
			println("\t" + entry.getKey() + "=" + entry.getValue());
		}
	}

	@Override
	public void caseLockThreadLineInfo(LockThreadLineInfo p) {
		if (!printLocks)
			return;
		String type = (p.getType().equals("locked")) ? "a " : "";
		String attr = "<" + p.getId() + "> (" + type + p.getClassName() + ")";
		println("\t- locked " + attr);
	}

	@Override
	public void caseLockStandaloneInfo(LockStandaloneInfo p) {
		if (!printStandaloneLocks)
			return;
		String type = (p.getType().equals("locked")) ? "a " : "";
		String attr = "<" + p.getId() + "> (" + type + p.getClassName() + ")" + " tid=" + p.getThreadId()
				+ " threadResolved=" + Boolean.toString(p.isThreadResolved());
		println("\t- locked " + attr);
	}

	@Override
	public void caseClassHistogramInfo(ClassHistogramInfo p) {
		println("ClassHistogramInfo");
		for (ClassHistogramItemInfo elt : p.getClassItems()) {
			caseClassHistogramItemInfo(elt);
		}
		println("Total" + " " + pad(8, Integer.toString(p.getTotalInstanceCount())) + "  "
				+ pad(10, Long.toString(p.getTotalInstanceByteSize())));
	}

	public void caseClassHistogramItemInfo(ClassHistogramItemInfo p) {
		// formatted with spaces as original input
		// println(p.getRank() + ": " + p.getInstanceCount() + " +
		// p.getInstanceByteSize() + " " + p.getClassName());
		println(pad(3, Integer.toString(p.getRank())) + ":" + "  " + pad(8, Integer.toString(p.getInstanceCount()))
				+ "  " + pad(10, Long.toString(p.getInstanceByteSize())) + "  " + p.getClassName());
	}

	private static String pad(int size, String value) {
		char[] ch = new char[size];
		Arrays.fill(ch, 0, size, (char) ' ');
		char[] chValue = value.toCharArray();
		int chValueLen = chValue.length;
		if (chValueLen < size) {
			System.arraycopy(chValue, 0, ch, size - chValueLen, chValueLen);
			return new String(ch);
		} else {
			return value;
		}
	}

	protected void print(String text) {
		out.print(text);
	}

	protected void println(String text) {
		out.println(text);
	}

}
