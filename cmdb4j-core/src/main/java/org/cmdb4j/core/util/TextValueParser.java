package org.cmdb4j.core.util;

/**
 * TODO use java.text.Format, for method Format.parseObject() instead ??
 * 
 * @param <T>
 */
public interface TextValueParser<T> {

    public T parse(String text);
    
}