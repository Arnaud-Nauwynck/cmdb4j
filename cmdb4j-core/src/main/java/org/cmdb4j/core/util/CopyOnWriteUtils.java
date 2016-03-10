package org.cmdb4j.core.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public final class CopyOnWriteUtils {

    private CopyOnWriteUtils() {
    }

    public static <T> List<T> immutableCopyWithAdd(List<T> ls, T elt) {
        return ImmutableList.<T>builder().addAll(ls).add(elt).build();
    }

    public static <T> List<T> immutableCopyWithRemove(List<T> ls, T elt) {
        ArrayList<T> tmp = new ArrayList<T>(ls);
        tmp.remove(elt);
        return ImmutableList.copyOf(tmp);
    }
}
