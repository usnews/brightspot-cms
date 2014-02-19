package com.psddev.cms.db.style;

public enum TextAlign {

    CENTER("center", "Center"),
    RIGHT("right", "Right"),
    JUSTIFY("justify", "Justify");

    private final String cssPropertyValue;
    private final String label;

    private TextAlign(String cssPropertyValue, String label) {
        this.cssPropertyValue = cssPropertyValue;
        this.label = label;
    }

    public String getCssPropertyValue() {
        return cssPropertyValue;
    }

    @Override
    public String toString() {
        return label;
    }
}
