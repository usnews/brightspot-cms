package com.psddev.cms.db;

import com.psddev.dari.db.Record;

public class ReferentialTextMarker extends Record {

    @Required
    @Indexed(unique = true)
    private String displayName;

    @Required
    @Indexed(unique = true)
    private String internalName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }
}
