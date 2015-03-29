package org.cmdb4j.core.util;

/**
 * TODO standard in JDK ???
 * (cf JavaBean PropertyEditor ?)
 *
 * @param <T>
 */
public interface TextValueParser<T> {

    public T parse(String text);
    
}