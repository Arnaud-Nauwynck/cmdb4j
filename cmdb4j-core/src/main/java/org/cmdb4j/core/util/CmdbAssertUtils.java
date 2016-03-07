package org.cmdb4j.core.util;

public final class CmdbAssertUtils {

    private CmdbAssertUtils() {
    }
    
    public static void checkNotNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("expected not null");
        }
    }
    
    
}
