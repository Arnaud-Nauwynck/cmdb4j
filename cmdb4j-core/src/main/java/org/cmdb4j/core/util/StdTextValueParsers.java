package org.cmdb4j.core.util;

/**
 * TODO standard in JDK ???
 * (cf JavaBean PropertyEditor ?)
 *
 */
public class StdTextValueParsers {

    @SuppressWarnings("unchecked")
    public static <T> TextValueParser<T> stdParserFor(Class<T> clss) {
        TextValueParser<?> res = null;
        if (clss.getName().startsWith("java.lang.")) {
            if (clss == String.class) {
                res = StringValueParser.INSTANCE;
            } else if (clss == Integer.class || clss == int.class) {
                res = IntValueParser.INSTANCE;
            } else if (clss == Long.class || clss == long.class) {
                res = LongValueParser.INSTANCE;
            } else {
                res = null; // unrecognized
            }
        }
        return (TextValueParser<T>) res;
    }
    
    public static class StringValueParser implements TextValueParser<String> {
        public static final StringValueParser INSTANCE = new StringValueParser();
        public String parse(String text) {
            return text;
        }
    }

    public static class IntValueParser implements TextValueParser<Integer> {
        public static final IntValueParser INSTANCE = new IntValueParser();
        public Integer parse(String text) {
            return Integer.parseInt(text);
        }
    }

    public static class LongValueParser implements TextValueParser<Long> {
        public static final LongValueParser INSTANCE = new LongValueParser();
        public Long parse(String text) {
            return Long.parseLong(text);
        }
    }

}