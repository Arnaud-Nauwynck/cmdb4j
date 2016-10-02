package org.cmdb4j.core.ext.threaddumps.util;

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A support class for writing XML files.
 *
 * inspired from jcommon
 */
public class XmlWriter {

    /** A constant for close. */
    public static boolean CLOSE = true;

    /** A constant for open. */
    public static boolean OPEN = false;

    /**The line separator. */
    private static String lineSeparator;
    static {
//        try {
//            lineSeparator = System.getProperty("line.separator", "\n");
//        } catch (Exception se) {
//            lineSeparator = "\n";
//            // ignoe, no rethrow
//        }
    	lineSeparator = "\n";
    }

    public static String getLineSeparator() { 
    	return lineSeparator;
    }

    //*************************************************************************

    
    /** the output for writng */
    protected Writer writer;
    
    /** The indent level for that writer. */
    private int indentLevel;

    /** The indent string. */
    private String indentString;

    /** 
     * A flag indicating whether to force a linebreak before printin the next 
     * tag. 
     */
    private boolean newLineOk;

    //*************************************************************************
    
    /**
     * Default Constructor. The created XMLWriterSupport will not have no safe 
     * tags and stars with an indention level of 0.  
     */
    public XmlWriter(Writer writer) {
        this(writer, 0, " ");
    }

    /**
     * Creates a new support instance.
     *
     * @param safeTags  the tags that are safe for line breaks.
     * @param indentLvel  the indent level.
     * @param indentString  the indent string.
     */
    public XmlWriter(Writer writer, int indentLevel, String indentString) {
        if (indentString == null) {
            throw new NullPointerException("IndentString must not be null");
        }
        this.writer = writer;
        this.indentLevel = indentLevel;
        this.indentString = indentString;
    }

    public void writeXmlDeclaration() throws IOException {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        println();
    }

    /**
     * Writes some text to the character stream.
     *
     * @param text the text.
     * @throws IOException if there is a problem writing to the characterstream.
     */
    public void writeText(String text) throws IOException {
        writer.write(text);
    }

    /**
     * Writes some text to the character stream.
     *
     * @param text the text.
     * @throws IOException if there is a problem writing to the characterstream.
     */
    public void writeTextNormalized(String text) throws IOException {
        writer.write(normalize(text));
    }

    /**
     * Writes an opening XML tag that has no attributes.
     *
     * @param w  the writer.
     * @param name  the tag name.
     *
     * @throws java.io.IOxception if there is an I/O problem.
     */
    public void writeTag(String name) throws IOException {
        if (this.newLineOk) {
            println();
        }
        printIndentIncrease();

        writer.write("<");
        writer.write(name);
       writer.write(">");
        if (isNewlineAfterOpenTag(name)) {
            println();
        }
    }

    /**
     * Writes a closing XML tag.
     *
     * @param w  the writer.
     * @param tag  the tag name.
     *
     * @throws java.io.IOExcepion if there is an I/O problem.
     */
    public void writeCloseTag(String tag) throws IOException {
        if (this.newLineOk || isNewlineAfterOpenTag(tag)) {
            if (this.newLineOk) {
                println();
            }
            printIndentDecrease();
        }
        else {
            decreaseIndent();
        }
        writer.write("</");
        writer.write(tag);
        writer.write(">");
        if (isNewlineAfterCloseTag(tag)) {
            println();
        }
        this.newLineOk = false;
    }

    /**
     * Writes an opening XML tag with an attribute/value pair.
     *
     * @param w  the writer.
     * @param name  the tag name.
     * @param attributeName  the attribute name.
     * @param attributeValue  the attribute valu.
     * @param close  controls whether the tag is closed.
     *
     * @throws java.io.IOException if there is an I/O problem.
     */
    public void writeTag(String name, String attributeName, String attributeValue, boolean close) throws IOException {
       AttributesImpl attr = null;
    	if (attributeName != null) {
            attr = new AttributesImpl();
    		attr.addAttribute(null, attributeName, null, null, attributeValue);
        }
        writeTag(name, attr, close);
    }

    /**
     * Writes a opening XML tag along with a list of attribute/value pairs.
     *
     * @param w  the writer.
     * @param name  the tag name.
     * @param attributes  the attributes.
     * @param close  controls whether the tag is closed.
     *
     * @throws java.ioIOException if there is an I/O problem.     
     */
    public void writeTag(String name, Attributes attributes, boolean close) throws IOException {
        if (this.newLineOk) {
            println();
            this.newLineOk = false;
        }
        printIndentIncrease();

        writer.write("<");
        writer.write(name);
        if (attributes != null && attributes.getLength() != 0) {
	        int size = attributes.getLength();
        	for (int i = 0; i < size; i++) {
	            String key = attributes.getLocalName(i);
	            String value = attributes.getValue(i);
	            writer.write(" ");
	            writer.write(key);
	            writer.write("=\"");
	            writer.write(normalize(value));
	            writer.write("\"");
	        }
        }
        if (close) {
            writer.write("/>");
            if (isNewlineAfterCloseTag(name)) {
                println();
            }
            decreaseIndent();
        }
        else {
            writer.write(">");
            if (isNewlineAfterOpenTag(name)) {
                println();
            }
        }
    }


    public void printIndent() throws IOException {
    	for (int i = 0; i < indentLevel; i++) {
            writer.write(indentString);
        }
    }
    
    public void println() throws IOException {
    	writer.write(getLineSeparator());
    }
    
    protected boolean isNewlineAfterOpenTag(String name) {
    	// TOADD tagIndentInfo (SafeTagList not supported yet)
//    	return tagIndentInfo!=null || tagIndentInfo.isafeForOpen(name));
    	return true;
    }
    
    protected boolean isNewlineAfterCloseTag(String name) {
    	// TOADD tagIndentInfo (SafeTagList not supported yet)
//    	return tagIndentInfo!=null || tagIndentInfo.isSafeForclose(name);
    	return true;
    }
    
    public void printIndentIncrease() throws IOException {
    	printIndent();
    	increaseIndent();
    }

    public void printIndentDecrease() throws IOException {
    	decreaseIndent();
    	printIndent();
    }

    /**
     * Returns te current indent level.
     *
     * @return the current indent level.
     */
    public int getIndentLevel() {
        return this.indentLevel;
    }

    /**
     * Increases the indention by one level.
     */
    public void increaseIndent() {
       this.indentLevel++;
    }

    /**
     * Decreates the indention by one level.
     */
    public void decreaseIndent() {
        this.indentLevel--;
    }

        
    /**
     * Starts a new block by increasing the indent level.
     *
     * @trows IOException if an IO error occurs.
     */
    public void increaseIndentLineBreak() throws IOException {
        this.indentLevel++;
        allowLineBreak();
    }

    /**
     * Ends the current block by decreasing the indent level.
     *
     * throws IOException if an IO error occurs.
     */
    public void decreaseIndentLineBreak() throws IOException {
        this.indentLevel--;
        allowLineBreak();
    }

    /**
     * Forces a linebreak on the next call to writeTag or writeCloseTag.
    *
     * @throws IOException if an IO error occurs.
     */
    public void allowLineBreak() throws IOException {
        this.newLineOk = true;
    }

    
    //*************************************************************************
    
    /**
    * Normalises a string, replacing certain characters with their escape 
     * sequences so that the XML text is not corrupted.
     *
     * @param s  the string.
     *
     * @return the normalised string.
     */
    public static String normalize(String s) {
        if (s == null) {
            return "";
        }
        StringBuffer str = new StringBuffer();
        int len = s.length();

        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);

            switch (ch) {
               case '<':
                    {
                        str.append("&lt;");
                        break;
                    }
                case '>':
                    {
                        str.append("&gt;");
                        break;
                   }
                case '&':
                    {
                        str.append("&amp;");
                        break;
                    }
                case '"':
                    {
                        str.append("&quot;");
                        break;
                    }
                case '\n':
                    {
                        if (i > 0) {
                            char lastChar = str.charAt(str.length() - 1);

                            if (lastChar != 'r') {
                                str.append(getLineSeparator());
                            }
                            else {
                                str.append('\n');
                            }
                        }
                       else {
                            str.append(getLineSeparator());
                        }
                        break;
                    }
                default :
                    {
                        str.append(ch);
                   }
            }
        }

        return (str.toString());
    }
    
}

