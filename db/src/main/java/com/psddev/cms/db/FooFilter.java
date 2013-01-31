package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import com.psddev.dari.util.AbstractFilter;

public class FooFilter extends AbstractFilter {

    @Override
    public Iterable<Class<? extends Filter>> dependencies() {
        List<Class<? extends Filter>> dependencies = new ArrayList<Class<? extends Filter>>();
        // dependencies.add(com.psddev.dari.db.CachingDatabaseFilter.class);
        return dependencies;
    }
}
