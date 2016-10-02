package org.cmdb4j.core.ext.threaddumps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cmdb4j.core.ext.threaddumps.analyzer.EjbSkelMethodLineRemover;
import org.cmdb4j.core.ext.threaddumps.analyzer.InactiveThreadRemover;
import org.cmdb4j.core.ext.threaddumps.analyzer.MethodCategory;
import org.cmdb4j.core.ext.threaddumps.analyzer.MethodLineRemover;
import org.cmdb4j.core.ext.threaddumps.analyzer.SystemThreadRemover;
import org.cmdb4j.core.ext.threaddumps.model.ClassHistogramInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpList;
import org.cmdb4j.core.ext.threaddumps.model.ThreadItemInfoVisitor;
import org.cmdb4j.core.ext.threaddumps.parser.ThreadDumpListParser;
import org.cmdb4j.core.ext.threaddumps.parser.sourcereader.SourceReader;
import org.cmdb4j.core.ext.threaddumps.parser.sourcereader.SourceReaderFactory;
import org.cmdb4j.core.ext.threaddumps.util.ClassHistogramInfoListIncrPrinter;
import org.cmdb4j.core.ext.threaddumps.util.ClassHistogramInfoListPrinter;
import org.cmdb4j.core.ext.threaddumps.util.DefaultThreadDumpPrinter;
import org.cmdb4j.core.ext.threaddumps.util.XmlThreadDumpPrinter;
import org.cmdb4j.core.ext.threaddumps.util.XmlWriter;


/**
 * Thread Dump extractor
 */
public class Main {

    private static final String NL = "\n";

	// -------------------------------------------------------------------------
    
	private String inputFile;
	private File outputDir;
	private String outputFileNamePrefix;
	private String outputThreadDumpSuffix = "-threaddump";

	private boolean dumpFull = false;
	private boolean dumpShort = true;

	/**
	 * format for output: one of "txt" / "xml"
	 */
	private String dumpFormat = "txt";
	
	
	// -------------------------------------------------------------------------

    public Main() {
    }

	// -------------------------------------------------------------------------
    
    public static void main(String args[]) throws IOException {
    	try {
            System.out.println(NL + "ThreadDumpAnalyzerMain Started at: " + new Date());

           new Main().run(args);

            System.out.println(NL + "ThreadDumpAnalyzerMain Finished at: " + new Date());
            System.exit(0);
    	} catch(Exception ex) {
    		System.err.println(NL + "ERROR - ThreadDumpAnalyzerMain Finished at: " + new Date());
    		ex.printStackTrace(System.err);
    		System.exit(1);
    	}
    }
    
    public void run(String args[]) throws IOException {
    	
        if(args.length < 1)
        {
            // System.err.println("Article: http://dev2dev.bea.com/produts/wlplatform81/articles/thread_dumps.jsp");
            System.err.println("Usage: java -jar <<threaddump.jar>> <input_dumpfile> [--out <output_fileprefix>][--dump-full][--no-dump-short][--format txt/xml]");
            System.exit(1);
        }

        inputFile = args[0];

        for (int i = 1; i < args.length; i++) {
        	String opt = args[i];
        	if (opt.equals("--out")) {
        		if (i+1>=args.length) throw new IllegalArgumentException("missing arg");
        		String arg = args[++i];
        		outputFileNamePrefix = arg;
        	} else if (opt.equals("--dump-full")) {
        		dumpFull = true;
        	} else if (opt.equals("--no-dump-short")) {
        		dumpShort = true;
        	} else if (opt.equals("--format")) {
        		if (i+1>=args.length)throw new IllegalArgumentException("missing arg");
        		String arg = args[++i];
        		dumpFormat = arg;
        	} else {
        		throw new IllegalArgumentException("unrecognized option '" + opt + "'");
        	}
        }
        
       	if (outputFileNamePrefix == null) {
//            println("Output fileName prefix is missing. Assuming 'dump'");
//            if (new File(inputFile).isDir()) {
//            	outputFilePrefix = args[1] + "dump";
//            }
        	outputFileNamePrefix = new File(inputFile).getName();
        	int indexLastDot = outputFileNamePrefix.lastIndexOf('.');
        	if (indexLastDot != -1) {
        		outputFileNamePrefix = outputFileNamePrefix.substring(0, indexLastDot);
        	}
       	}

        outputDir = new File(inputFile).getParentFile();
        
        
        ThreadDumpList threadDumpList = new ThreadDumpList();
        
        // Parsing
        println("Parsing ThreadDumps started at: " + new Date());
        println("Extracting from: " + inputFile);
        println("Output dir is:" + outputDir);
        println("Output file name prefix is: " + outputFileNamePrefix);
        
    	SourceReader sourcereader = SourceReaderFactory.getSourceReader(inputFile);
        ThreadDumpListParser parser = new ThreadDumpListParser(threadDumpList, sourcereader); 
        try {
	        parser.parse();
        } finally {
        	if (sourcereader != null) sourcereader.close();
        }
        
        println(NL + "Parsing ThreadDumps finished at: " + new Date());
        if(threadDumpList.getThreadDumps().size() == 0) {
            println(NL + "No Dumps found");
        }
        
        // TODO analyse ThreadDump to print summary ...
        
        
        // Dump Full (List<ThreadDump> before simplification) 
        if (dumpFull) {
        	dump(threadDumpList, outputDir, outputFileNamePrefix, inputFile);
        }

        
        // remove System threads: "Finalizer", "Reference Handler", ...
        threadDumpList.visit(new SystemThreadRemover());

        // remove iddle threas
        InactiveThreadRemover inactiveThreadRemover = new InactiveThreadRemover();
        threadDumpList.visit(inactiveThreadRemover);
        
        // remove intermediate stack entry for EJB skeletons
        threadDumpList.visit(new EjbSkelMethodLineRemover());
        
        MethodLineRemover methodLineRemover = new MethodLineRemover(MethodCategory.DEFAULT_RULES);
        threadDumpList.visit(methodLineRemover);
        
        
        
        // Dump Short (List<ThreadDump> after simplification) 
       if (dumpShort) {
        	dump(threadDumpList, outputDir, outputFileNamePrefix+"-short", inputFile);
        }
        
        if (threadDumpList.getThreadDumps().size() != 0 && threadDumpList.get(0).getClassHistogramInfo() != null) {
        	// dump clasHistograms in csv file
        	List<ClassHistogramInfo> ls = new ArrayList<ClassHistogramInfo>();
        	for(ThreadDumpInfo td : threadDumpList.getThreadDumps()) {
        		if (td.getClassHistogramInfo() != null) {
        			ls.add(td.getClassHistogramInfo());
        		}
        	}
        	File fileClassHistoByteSize = new File(outputDir, outputFileNamePrefix+ "-classHisto-size.csv");
        	ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoByteSize, ls, true, false);

//        	File fileClasHistoByteSizeIncr = new File(outputDir, outputFileNamePrefix+ "-classHisto-size-incr.csv");
//        	ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoByteSizeIncr, ls, true, true);

        	File fileClassHistoInstanceCount = new File(outputDir, outputFileNamePrefix+ "-classHisto-count.csv");
        	ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoInstanceCount, ls, false, false);
        	
//        	File fileClassHistoInstanceCountIncr = new File(outputDir, outputFileNamePrefix+ "-cassHisto-count-incr.csv");
//        	ClassHistogramInfoListPrinter.dumpClassHistoCsvFile(fileClassHistoInstanceCountIncr, ls, false, true);

        	
        	File fileClassHistoByteSizeIncr2 = new File(outputDir, outputFileNamePrefix+ "-classHisto-size-incr-cass.csv");
        	ClassHistogramInfoListIncrPrinter.dumpClassHistoCsvFile(fileClassHistoByteSizeIncr2, ls, true);

        	File fileClassHistoInstanceCountIncr2 = new File(outputDir, outputFileNamePrefix+ "-classHisto-count-incr-class.csv");
        	ClassHistogramInfoListIncrPrinter.dumpClassHistoCsvFile(fileClassHistoInstanceCountIncr2, ls, true);

        }
    }

	private void dump(ThreadDumpList threadDumpList, File outputDir, String outputFileName, String sourceName) throws IOException {
        if (dumpFormat.equals("xml")) {
        	dumpXml(threadDumpList, outputDir, outputFileName, sourceName);
        } else if (dumpFormat.equals("txt")) {
        	dumpTxt(threadDumpList, outputDir, outputFileName, sourceName);
        } else {
        	System.err.println("unreognized print format '" + dumpFormat + "', expecting 'txt' or 'xml' ... using default : 'txt'");
        	dumpTxt(threadDumpList, outputDir, outputFileName, sourceName);
        }
    }
    
    private void dumpXml(ThreadDumpList threadDumpList, File outputDir, String outputFileName, String sourceName) throws IOException {
        File file = new File(outputDir, outputFileName + outputThreadDumpSuffix + ".xml");
    	PrintWriter printwriter = null;
        try {
        	printwriter = new PrintWriter(new FileWriter(file));
        	XmlWriter xmlWriter = new XmlWriter(printwriter);
        	
        	ThreadItemInfoVisitor printVisitor = new XmlThreadDumpPrinter(xmlWriter);
        	printVisitor.caseThreadDumpList(threadDumpList);

            printwriter.flush();
           
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void dumpTxt(ThreadDumpList threadDumpList, File outputDir, String outputFileName, String sourceName) throws IOException {
    	File file = new File(outputDir, outputFileName + outputThreadDumpSuffix + ".txt");
    	PrintWriter printwriter = null;
        try {
        	printwriter = new PrintWriter(new FileWriter(file));
        	XmlWriter xmlWriter = new XmlWriter(printwriter);

        	ThreadItemInfoVisitor printVisitor = new DefaultThreadDumpPrinter(xmlWriter);
        	printVisitor.caseThreadDumpList(threadDumpList);

            printwriter.flush();
            
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void println(String s) {
        System.out.println(s);
    }

}
    


