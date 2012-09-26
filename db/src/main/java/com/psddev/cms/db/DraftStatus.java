package com.psddev.cms.db;

import com.psddev.dari.db.Record;

/** Status applied to {@linkplain Draft drafts} to control workflow. */
public class DraftStatus extends Record {

    @Indexed(unique = true)
    @Required
    private String name;

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }
}
