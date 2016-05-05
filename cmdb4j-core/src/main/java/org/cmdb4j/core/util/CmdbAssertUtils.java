package org.cmdb4j.core.util;

public final class CmdbAssertUtils {

    private CmdbAssertUtils() {
    }
    
    public static void checkNotNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("expected not null");
        }
    }
    
    public static void checkNotNull(Object obj1, Object obj2) {
        checkNotNull(obj1);
        checkNotNull(obj2);
    }

    public static void checkNotNull(Object obj1, Object obj2, Object obj3) {
        checkNotNull(obj1);
        checkNotNull(obj2);
        checkNotNull(obj3);
    }

    public static void checkNotNull(Object... objs) {
        for (Object obj : objs) {
            checkNotNull(obj);
        }
    }

}
