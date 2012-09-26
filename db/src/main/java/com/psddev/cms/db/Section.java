package com.psddev.cms.db;

import com.psddev.dari.db.Record;

import java.util.LinkedHashMap;
import java.util.Map;

public class Section extends Record {

    private String name;
    private @Indexed boolean isShareable;
    private boolean isCacheable;
    private long cacheDuration;

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns {@code true} if this section is shareable across multiple
     * pages.
     */
    public boolean isShareable() {
        return isShareable;
    }

    /** Sets whether this section is shareable across multiple pages. */
    public void setShareable(boolean isShareable) {
        this.isShareable = isShareable;
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    public Map<String, Object> toDefinition() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("_id", getId().toString());
        map.put("_type", getClass().getName());
        map.put("name", getName());
        map.put("isShareable", isShareable());
        map.put("cacheDuration", getCacheDuration());
        return map;
    }
}
