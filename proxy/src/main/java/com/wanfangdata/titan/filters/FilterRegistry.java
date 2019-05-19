package com.wanfangdata.titan.filters;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class FilterRegistry {

    private static final FilterRegistry instance = new FilterRegistry();

    public static final FilterRegistry instance() {
        return instance;
    }

    private final ConcurrentHashMap<String, TitanFilter> filters = new ConcurrentHashMap<String, TitanFilter>();

    private FilterRegistry() {
    }

    public TitanFilter remove(String key) {
        return this.filters.remove(key);
    }

    public TitanFilter get(String key) {
        return this.filters.get(key);
    }

    public void put(String key, TitanFilter filter) {
        this.filters.putIfAbsent(key, filter);
    }

    public int size() {
        return this.filters.size();
    }

    public Collection<TitanFilter> getAllFilters() {
        return this.filters.values();
    }

}
