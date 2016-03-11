package org.cmdb4j.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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

    public static <K,V> Map<K,V> immutableCopyWithPut(Map<K,V> map, K key, V value) {
        // if (map.containsKey(key)) { // if override... (dont use guava Map.Builder)
        // }
        LinkedHashMap<K,V> tmp = new LinkedHashMap<>(map);
        tmp.put(key, value);
        return ImmutableMap.copyOf(tmp);
    }

    public static <K,V> Map<K,V> immutableCopyWithRemove(Map<K,V> map, K key) {
        LinkedHashMap<K,V> tmp = new LinkedHashMap<>(map);
        tmp.remove(key);
        return ImmutableMap.copyOf(tmp);
    }

}
