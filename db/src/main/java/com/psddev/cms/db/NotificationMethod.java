package com.psddev.cms.db;

public enum NotificationMethod {

    EMAIL("Email"),
    SMS("Text Message");

    private String displayName;

    private NotificationMethod(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
