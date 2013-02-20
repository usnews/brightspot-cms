package com.psddev.cms.db;

/** Standard content statuses. */
public enum ContentStatus {

    DISABLED("Disabled"),
    DELETED("Deleted");

    private String displayName;

    private ContentStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
