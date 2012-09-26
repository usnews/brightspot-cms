package com.psddev.cms.db;

import com.psddev.dari.db.Query;

import java.util.HashMap;
import java.util.Map;

public class SearchQuery extends Query<Object> {

    private Map<String, String> substitutions;

    protected SearchQuery() {
        super(null, null);
    }

    public Map<String, String> getSubstitutions() {
        if (substitutions == null) {
            setSubstitutions(new HashMap<String, String>());
        }
        return substitutions;
    }

    public void setSubstitutions(Map<String, String> substitutions) {
        this.substitutions = substitutions;
    }
}
