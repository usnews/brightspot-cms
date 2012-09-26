package com.psddev.cms.tool;

public enum SearchSort {

    RELEVANT("Relevant"),
    NEWEST("Newest");

    private final String label;

    SearchSort(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
